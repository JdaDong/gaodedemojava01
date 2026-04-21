package org.example.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Caffeine 本地缓存配置。
 *
 * <p>Spring Cache 默认全局一个 TTL，无法满足"不同业务缓存时长不同"的需求，
 * 因此手动构造 {@link SimpleCacheManager}，为每个 cache name 指定独立的 Caffeine 规格。</p>
 *
 * <h3>缓存 region 一览</h3>
 * <table>
 *   <tr><th>cache name</th><th>TTL</th><th>最大条目</th><th>用途</th></tr>
 *   <tr><td>poi</td><td>10 min</td><td>500</td><td>POI 搜索结果</td></tr>
 *   <tr><td>weatherLive</td><td>5 min</td><td>500</td><td>实时天气（高德自身也是分钟级）</td></tr>
 *   <tr><td>weatherForecast</td><td>30 min</td><td>500</td><td>3 天天气预报</td></tr>
 *   <tr><td>geocode</td><td>24 h</td><td>1000</td><td>地址 → 坐标</td></tr>
 *   <tr><td>regeocode</td><td>24 h</td><td>1000</td><td>坐标 → 地址</td></tr>
 * </table>
 *
 * <p>驾车路径规划 {@code drivingRoute} 受实时路况影响，故意<b>不加缓存</b>。</p>
 *
 * <p>统计：用 {@code recordStats()} 打开命中率统计，后续可通过
 * {@code cacheManager.getCache("poi").getNativeCache()} 拿到 {@code com.github.benmanes.caffeine.cache.Cache}
 * 并调用 {@code stats()} 观察命中率。</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    public static final String CACHE_POI = "poi";
    public static final String CACHE_WEATHER_LIVE = "weatherLive";
    public static final String CACHE_WEATHER_FORECAST = "weatherForecast";
    public static final String CACHE_GEOCODE = "geocode";
    public static final String CACHE_REGEOCODE = "regeocode";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache(CACHE_POI,              Duration.ofMinutes(10), 500),
                buildCache(CACHE_WEATHER_LIVE,     Duration.ofMinutes(5),  500),
                buildCache(CACHE_WEATHER_FORECAST, Duration.ofMinutes(30), 500),
                buildCache(CACHE_GEOCODE,          Duration.ofHours(24),  1000),
                buildCache(CACHE_REGEOCODE,        Duration.ofHours(24),  1000)
        ));
        log.info("[CacheConfig] Caffeine 缓存已初始化: {} 个 region", 5);
        return manager;
    }

    private static CaffeineCache buildCache(String name, Duration ttl, long maxSize) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .expireAfterWrite(ttl)
                        .maximumSize(maxSize)
                        .recordStats()     // 开启命中率统计
                        .build());
    }
}
