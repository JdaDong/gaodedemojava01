package org.example;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.config.AmapProperties;
import org.example.model.Geocode;
import org.example.model.Poi;
import org.example.model.ReGeocode;
import org.example.model.Route;
import org.example.model.TransitRoute;
import org.example.model.WeatherForecast;
import org.example.model.WeatherLive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.config.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 高德地图 Web 服务 API 客户端
 * <p>
 * 封装了以下功能：
 * <ul>
 *     <li>POI 搜索：根据关键词查询地点，返回 {@link Poi} 列表</li>
 *     <li>天气查询：实时 {@link WeatherLive} / 预报 {@link WeatherForecast}</li>
 *     <li>路径规划：驾车 / 步行 / 骑行，返回 {@link Route}；公交换乘返回 {@link TransitRoute}</li>
 *     <li>地理编码：地址 → {@link Geocode} / 经纬度 → {@link ReGeocode}</li>
 * </ul>
 * <p>
 * 使用前请到 https://lbs.amap.com/ 申请 "Web 服务" 类型的 Key
 * <p>
 * 两种使用方式：
 * <ul>
 *     <li><b>Spring 环境</b>：自动装配，直接 {@code @Autowired AmapClient amapClient}</li>
 *     <li><b>纯 Java 环境</b>：手动 {@code new AmapClient(apiKey)}，见 {@link Main}</li>
 * </ul>
 * <p>
 * <b>缓存：</b>仅在 Spring 环境下生效（通过 Spring Cache 代理）。CLI 直接 {@code new} 的
 * 实例不会走代理，不会命中缓存。具体缓存规格见 {@link org.example.config.CacheConfig}。
 */
@Service
public class AmapClient {

    private static final Logger log = LoggerFactory.getLogger(AmapClient.class);

    /** 高德 Web 服务 API 的默认基础地址 */
    private static final String DEFAULT_BASE_URL = "https://restapi.amap.com/v3";

    private final String apiKey;

    /** API 基地址（可注入，仅供单测指向 MockWebServer） */
    private final String baseUrl;

    /** OkHttp 客户端（复用连接池，提升性能） */
    private final OkHttpClient httpClient;

    public AmapClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    /**
     * Spring 注入用构造器：根据 {@link AmapProperties} 创建。
     * <p>
     * Spring 容器启动时会优先使用此构造器（唯一参数是容器里的 Bean {@code AmapProperties}）。
     */
    @Autowired
    public AmapClient(AmapProperties properties) {
        if (properties.key() == null || properties.key().isBlank()) {
            throw new IllegalStateException(
                    "amap.key 未配置。请在 application.yml 中填写，或通过环境变量 AMAP_KEY 提供。");
        }
        this.apiKey = properties.key();
        this.baseUrl = properties.baseUrl();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.connectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.readTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
        log.info("[AmapClient] 初始化完成, baseUrl={}, connectTimeout={}s, readTimeout={}s",
                baseUrl, properties.connectTimeoutSeconds(), properties.readTimeoutSeconds());
    }

    /**
     * 包级别构造器，仅供测试注入自定义 baseUrl（如 MockWebServer 的地址）。
     */
    AmapClient(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    // =====================================================================
    // 1. POI 搜索
    // 文档：https://lbs.amap.com/api/webservice/guide/api/search
    // =====================================================================

    /**
     * POI 关键词搜索
     *
     * @param keywords 搜索关键词，如 "肯德基"、"北京大学"
     * @param city     城市名或 adcode，如 "北京"、"010"，传 null 则全国搜索
     * @return POI 列表（可能为空，但不会为 null）
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_POI,
            key = "#keywords + ':' + (#city == null ? '' : #city)")
    public List<Poi> searchPoi(String keywords, String city) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/place/text").newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("keywords", keywords)
                .addQueryParameter("offset", "10")
                .addQueryParameter("page", "1")
                .addQueryParameter("extensions", "base");
        if (city != null && !city.isEmpty()) {
            urlBuilder.addQueryParameter("city", city);
        }
        JSONObject json = doGet(urlBuilder.build().toString());
        return AmapResponse.parse(json, "POI 搜索", j -> {
            JSONArray pois = j.getJSONArray("pois");
            return (pois == null || pois.isEmpty())
                    ? Collections.<Poi>emptyList()
                    : pois.toJavaList(Poi.class);
        }).getData();
    }

    // =====================================================================
    // 2. 天气查询
    // 文档：https://lbs.amap.com/api/webservice/guide/api/weatherinfo
    // =====================================================================

    /**
     * 查询指定城市的实时天气
     *
     * @param adcode 城市编码，如 "110000"（北京）
     * @return 实时天气，若无数据返回 null
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_WEATHER_LIVE, key = "#adcode",
            unless = "#result == null")
    public WeatherLive queryWeatherLive(String adcode) throws IOException {
        JSONObject json = doWeather(adcode, false);
        return AmapResponse.parse(json, "实时天气查询", j -> {
            JSONArray lives = j.getJSONArray("lives");
            return (lives == null || lives.isEmpty())
                    ? null
                    : lives.getJSONObject(0).to(WeatherLive.class);
        }).getData();
    }

    /**
     * 查询指定城市未来 3 天的天气预报
     *
     * @param adcode 城市编码
     * @return 天气预报，若无数据返回 null
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_WEATHER_FORECAST, key = "#adcode",
            unless = "#result == null")
    public WeatherForecast queryWeatherForecast(String adcode) throws IOException {
        JSONObject json = doWeather(adcode, true);
        return AmapResponse.parse(json, "天气预报查询", j -> {
            JSONArray forecasts = j.getJSONArray("forecasts");
            return (forecasts == null || forecasts.isEmpty())
                    ? null
                    : forecasts.getJSONObject(0).to(WeatherForecast.class);
        }).getData();
    }

    private JSONObject doWeather(String adcode, boolean isForecast) throws IOException {
        String url = HttpUrl.parse(baseUrl + "/weather/weatherInfo").newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("city", adcode)
                .addQueryParameter("extensions", isForecast ? "all" : "base")
                .build().toString();
        return doGet(url);
    }

    // =====================================================================
    // 3. 路径规划（驾车）
    // 文档：https://lbs.amap.com/api/webservice/guide/api/direction
    // =====================================================================

    /**
     * 驾车路径规划
     *
     * @param origin      起点经纬度，格式 "经度,纬度"
     * @param destination 终点经纬度，格式同上
     * @return 驾车路线结果
     */
    public Route drivingRoute(String origin, String destination) throws IOException {
        String url = HttpUrl.parse(baseUrl + "/direction/driving").newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("origin", origin)
                .addQueryParameter("destination", destination)
                .addQueryParameter("extensions", "base")
                .addQueryParameter("strategy", "0")   // 0=速度优先
                .build().toString();
        JSONObject json = doGet(url);
        return AmapResponse.parse(json, "驾车路径规划", j -> {
            JSONObject route = j.getJSONObject("route");
            return route == null ? null : route.to(Route.class);
        }).getData();
    }

    /**
     * 步行路径规划
     * <p>
     * 文档：{@code /v3/direction/walking}。适合短距离（起终点直线距离通常建议小于 100km）。
     *
     * @param origin      起点 "经度,纬度"
     * @param destination 终点 "经度,纬度"
     * @return 步行路线（内部结构同驾车）
     */
    public Route walkingRoute(String origin, String destination) throws IOException {
        String url = HttpUrl.parse(baseUrl + "/direction/walking").newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("origin", origin)
                .addQueryParameter("destination", destination)
                .build().toString();
        JSONObject json = doGet(url);
        return AmapResponse.parse(json, "步行路径规划", j -> {
            JSONObject route = j.getJSONObject("route");
            return route == null ? null : route.to(Route.class);
        }).getData();
    }

    /**
     * 骑行路径规划（⚠️ 使用高德 v4 接口，响应格式与 v3 不同）
     * <p>
     * 文档：{@code /v4/direction/bicycling}。
     * v4 响应结构：
     * <pre>
     * { "errcode": 0, "errmsg": "ok",
     *   "data": { "origin":..., "destination":..., "paths":[{distance, duration, steps:[...]}] } }
     * </pre>
     * 因其与 v3 的 {@code status/info/infocode} 不同，此处单独处理、不走 {@link AmapResponse}。
     *
     * @param origin      起点 "经度,纬度"
     * @param destination 终点 "经度,纬度"
     * @return 骑行路线（复用 {@link Route} 结构，path/step 字段兼容）
     */
    public Route bicyclingRoute(String origin, String destination) throws IOException {
        // 骑行接口的 base 是 v4，需要把 /v3 前缀替换掉
        String v4Base = baseUrl.replaceFirst("/v3$", "/v4");
        String url = HttpUrl.parse(v4Base + "/direction/bicycling").newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("origin", origin)
                .addQueryParameter("destination", destination)
                .build().toString();
        JSONObject json = doGet(url);
        if (json == null) {
            throw new AmapException("骑行路径规划", "EMPTY_RESPONSE", "响应为空");
        }
        // v4 版用 errcode 而不是 status，成功为 0
        Integer errcode = json.getInteger("errcode");
        if (errcode == null || errcode != 0) {
            String errmsg = json.getString("errmsg");
            throw new AmapException("骑行路径规划",
                    errcode == null ? "UNKNOWN" : String.valueOf(errcode),
                    errmsg == null ? "未知错误" : errmsg);
        }
        JSONObject data = json.getJSONObject("data");
        return data == null ? null : data.to(Route.class);
    }

    /**
     * 公交路径规划（含地铁 + 公交，支持跨城市）
     * <p>
     * 文档：{@code /v3/direction/transit/integrated}。
     *
     * @param origin      起点 "经度,纬度"
     * @param destination 终点 "经度,纬度"
     * @param city        起点所在城市名或 adcode（必填）
     * @param cityd       终点所在城市名或 adcode，跨城公交时必填，同城可传 null
     * @return 公交换乘方案集合
     */
    public TransitRoute transitRoute(String origin, String destination,
                                     String city, String cityd) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/direction/transit/integrated").newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("origin", origin)
                .addQueryParameter("destination", destination)
                .addQueryParameter("city", city)
                .addQueryParameter("strategy", "0");   // 0=最快捷
        if (cityd != null && !cityd.isEmpty()) {
            urlBuilder.addQueryParameter("cityd", cityd);
        }
        JSONObject json = doGet(urlBuilder.build().toString());
        return AmapResponse.parse(json, "公交路径规划", j -> {
            JSONObject route = j.getJSONObject("route");
            return route == null ? null : route.to(TransitRoute.class);
        }).getData();
    }

    // =====================================================================
    // 4. 地理编码 / 逆地理编码
    // 文档：https://lbs.amap.com/api/webservice/guide/api/georegeo
    // =====================================================================

    /**
     * 地理编码：结构化地址 → 经纬度
     * <p>
     * 示例："上海市徐汇区肇嘉浜路1065号" → 121.45xxx,31.19xxx，同时附带 adcode。
     *
     * @param address 结构化地址（必填）
     * @param city    指定查询的城市名/adcode，可选；传 null 则全国模糊匹配
     * @return 匹配结果列表（可能为空，但不会为 null）
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_GEOCODE,
            key = "#address + ':' + (#city == null ? '' : #city)")
    public List<Geocode> geocode(String address, String city) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/geocode/geo").newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("address", address);
        if (city != null && !city.isEmpty()) {
            urlBuilder.addQueryParameter("city", city);
        }
        JSONObject json = doGet(urlBuilder.build().toString());
        return AmapResponse.parse(json, "地理编码", j -> {
            JSONArray geocodes = j.getJSONArray("geocodes");
            return (geocodes == null || geocodes.isEmpty())
                    ? Collections.<Geocode>emptyList()
                    : geocodes.toJavaList(Geocode.class);
        }).getData();
    }

    /**
     * 逆地理编码：经纬度 → 结构化地址
     *
     * @param location 经纬度，格式 "经度,纬度"，如 "116.481028,39.989643"
     * @return 结构化地址，若无数据返回 null
     */
    @Cacheable(cacheNames = CacheConfig.CACHE_REGEOCODE, key = "#location",
            unless = "#result == null")
    public ReGeocode regeocode(String location) throws IOException {
        String url = HttpUrl.parse(baseUrl + "/geocode/regeo").newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("location", location)
                .addQueryParameter("extensions", "base")   // base=只返基础地址；all=含周边 POI/道路
                .build().toString();
        JSONObject json = doGet(url);
        return AmapResponse.parse(json, "逆地理编码", j -> {
            JSONObject regeocode = j.getJSONObject("regeocode");
            return regeocode == null ? null : regeocode.to(ReGeocode.class);
        }).getData();
    }

    // =====================================================================
    // 通用 GET 请求 & 状态校验
    // =====================================================================

    /** 发送 GET 请求并解析为 JSONObject（供高级用法/调试使用） */
    public JSONObject doGet(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        long start = System.currentTimeMillis();
        log.debug("[Amap] → GET {}", maskKey(url));
        try (Response response = httpClient.newCall(request).execute()) {
            long cost = System.currentTimeMillis() - start;
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("[Amap] ← HTTP {} 耗时 {}ms url={}", response.code(), cost, maskKey(url));
                throw new IOException("请求失败，HTTP 状态码: " + response.code());
            }
            String body = response.body().string();
            log.debug("[Amap] ← HTTP {} 耗时 {}ms 响应长度={}", response.code(), cost, body.length());
            return JSON.parseObject(body);
        }
    }

    /** 把 URL 里的 key 参数打码，避免日志泄漏 */
    private static String maskKey(String url) {
        return url.replaceAll("key=[^&]+", "key=***");
    }
}
