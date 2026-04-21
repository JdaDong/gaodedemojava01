package org.example;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.config.AmapProperties;
import org.example.config.CacheConfig;
import org.example.model.Poi;
import org.example.model.WeatherLive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 {@code @Cacheable} 注解在 Spring 代理下生效：
 * 同一参数多次调用时，只打第一次请求，后续直接从 Caffeine 缓存返回。
 *
 * <p>用小型 Spring 上下文启用 {@link CacheConfig}，并用指向 {@link MockWebServer} 的
 * {@link AmapClient} Bean 模拟高德响应。</p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CacheConfig.class, AmapClientCacheTest.TestConfig.class},
        loader = AnnotationConfigContextLoader.class)
@DisplayName("AmapClient · Caffeine 缓存测试")
class AmapClientCacheTest {

    @Autowired
    private AmapClient amapClient;

    @Autowired
    private MockWebServer server;

    @Autowired
    private org.springframework.cache.CacheManager cacheManager;

    @BeforeEach
    void resetBeforeEach() {
        // 每个用例开始前清空所有缓存；MockWebServer 请求计数用 reqBefore 做差值隔离，无需清零
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    // ---------- 测试用 Spring 配置 ----------
    @Configuration
    static class TestConfig {

        @Bean(destroyMethod = "shutdown")
        MockWebServer mockWebServer() throws IOException {
            MockWebServer s = new MockWebServer();
            s.start();
            return s;
        }

        @Bean
        AmapClient amapClient(MockWebServer server) {
            String baseUrl = server.url("/v3").toString().replaceAll("/$", "");
            AmapProperties props = new AmapProperties("test-key", baseUrl, 5, 10);
            // 走 @Service 路径的 AmapProperties 构造器，@Cacheable 代理才会生效
            return new AmapClient(props);
        }
    }

    @Test
    @DisplayName("相同参数第 2 次调用不会发起 HTTP 请求")
    void searchPoi_secondCallHitsCache() throws Exception {
        String body = """
                {"status":"1","info":"OK","infocode":"10000","pois":[
                  {"id":"B1","name":"肯德基(和平店)","address":"西藏中路268号","location":"121.47,31.23"}
                ]}""";
        long reqBefore = server.getRequestCount();
        // 只 enqueue 1 个响应：若缓存失效，第 2 次调用会拿不到响应
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));

        List<Poi> first = amapClient.searchPoi("肯德基", "上海");
        List<Poi> second = amapClient.searchPoi("肯德基", "上海");

        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        assertThat(second).isSameAs(first);                       // Caffeine 直接返回同一引用
        assertThat(server.getRequestCount() - reqBefore).isEqualTo(1);  // ✨ 只打了 1 次 HTTP
    }

    @Test
    @DisplayName("不同参数不共享缓存")
    void searchPoi_differentArgsTriggerNewRequest() throws Exception {
        String body = """
                {"status":"1","info":"OK","infocode":"10000","pois":[]}""";
        long reqBefore = server.getRequestCount();
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));

        amapClient.searchPoi("肯德基", "上海");
        amapClient.searchPoi("麦当劳", "上海");

        assertThat(server.getRequestCount() - reqBefore).isEqualTo(2);
    }

    @Test
    @DisplayName("null 返回值不缓存（unless = #result == null）")
    void weatherLive_nullResultNotCached() throws Exception {
        String emptyBody = """
                {"status":"1","info":"OK","infocode":"10000","lives":[]}""";
        long reqBefore = server.getRequestCount();
        server.enqueue(new MockResponse().setBody(emptyBody).setResponseCode(200));
        server.enqueue(new MockResponse().setBody(emptyBody).setResponseCode(200));

        WeatherLive w1 = amapClient.queryWeatherLive("110000");
        WeatherLive w2 = amapClient.queryWeatherLive("110000");

        assertThat(w1).isNull();
        assertThat(w2).isNull();
        // null 不入缓存，两次都真正打了 MockWebServer
        assertThat(server.getRequestCount() - reqBefore).isEqualTo(2);
    }

    @Test
    @DisplayName("5 个 cache region 全部存在")
    void cacheManager_hasAllRegions() {
        assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrder(
                CacheConfig.CACHE_POI,
                CacheConfig.CACHE_WEATHER_LIVE,
                CacheConfig.CACHE_WEATHER_FORECAST,
                CacheConfig.CACHE_GEOCODE,
                CacheConfig.CACHE_REGEOCODE);
    }
}
