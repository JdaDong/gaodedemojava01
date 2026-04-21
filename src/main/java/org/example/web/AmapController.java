package org.example.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.example.AmapClient;
import org.example.model.Geocode;
import org.example.model.Poi;
import org.example.model.ReGeocode;
import org.example.model.Route;
import org.example.model.TransitRoute;
import org.example.model.WeatherForecast;
import org.example.model.WeatherLive;
import org.example.ratelimit.RateLimit;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 校验说明：
 * <ul>
 *   <li>{@code @NotBlank} 必填且非空白</li>
 *   <li>{@code @Pattern(COORDINATE)} 坐标格式校验：经度 -180~180，纬度 -90~90，各保留最多 6 位小数</li>
 *   <li>{@code @Pattern(ADCODE)} 6 位数字行政区划编码</li>
 * </ul>
 * 校验失败时由 {@link GlobalExceptionHandler} 统一转成 400 JSON。
 */

/**
 * 高德 API 的 REST 接口层。
 * <p>
 * 所有接口均为 {@code GET}，直接返回高德封装后的 POJO，Spring Boot 会自动用 Jackson 序列化为 JSON。
 * <p>
 * 业务异常 {@link org.example.AmapException} 和 {@link IOException} 由 {@link GlobalExceptionHandler}
 * 统一捕获并返回标准化错误 JSON。
 */
@Tag(name = "高德地图 API", description = "POI 搜索 / 天气查询 / 驾车路径规划 / 地理编码")
@Validated
@RestController
@RequestMapping("/api")
public class AmapController {

    /** 经纬度格式：形如 "116.397428,39.909230"。经度 [-180,180]，纬度 [-90,90]，各至多 6 位小数 */
    private static final String COORDINATE =
            "^-?((1[0-7]\\d)|(\\d{1,2}))(\\.\\d{1,6})?,-?((90)|([1-8]?\\d))(\\.\\d{1,6})?$";

    /** adcode 格式：6 位数字 */
    private static final String ADCODE = "^\\d{6}$";

    private final AmapClient amapClient;

    public AmapController(AmapClient amapClient) {
        this.amapClient = amapClient;
    }

    // =====================================================================
    // 1. POI 搜索
    // =====================================================================

    @Operation(summary = "POI 关键字搜索",
            description = "按关键字在指定城市搜索 POI（兴趣点），例如：上海的肯德基、北京的银行等。")
    @RateLimit(qps = 5, burst = 10)
    @GetMapping("/poi")
    public List<Poi> searchPoi(
            @Parameter(description = "查询关键字，必填", example = "肯德基", required = true)
            @RequestParam @NotBlank(message = "keywords 不能为空") String keywords,
            @Parameter(description = "城市名或 adcode，可选", example = "上海")
            @RequestParam(required = false) String city) throws IOException {
        return amapClient.searchPoi(keywords, city);
    }

    // =====================================================================
    // 2. 天气查询
    // =====================================================================

    @Operation(summary = "实时天气",
            description = "根据 adcode 查询实时天气，包含温度、湿度、风向风力等。")
    @RateLimit(qps = 10, burst = 20)
    @GetMapping("/weather/live")
    public WeatherLive weatherLive(
            @Parameter(description = "城市 adcode（6 位）", example = "110000", required = true)
            @RequestParam @Pattern(regexp = ADCODE, message = "adcode 必须是 6 位数字") String adcode)
            throws IOException {
        return amapClient.queryWeatherLive(adcode);
    }

    @Operation(summary = "天气预报",
            description = "根据 adcode 查询未来 3 天天气预报。")
    @GetMapping("/weather/forecast")
    public WeatherForecast weatherForecast(
            @Parameter(description = "城市 adcode（6 位）", example = "110000", required = true)
            @RequestParam @Pattern(regexp = ADCODE, message = "adcode 必须是 6 位数字") String adcode)
            throws IOException {
        return amapClient.queryWeatherForecast(adcode);
    }

    // =====================================================================
    // 3. 驾车路径规划
    // =====================================================================

    @Operation(summary = "驾车路径规划",
            description = "返回从起点到终点的驾车路径，包含总距离、耗时和分段导航。坐标格式：经度,纬度")
    @RateLimit(qps = 3, burst = 6)
    @GetMapping("/route/driving")
    public Route drivingRoute(
            @Parameter(description = "起点坐标（经度,纬度）", example = "116.397428,39.909230", required = true)
            @RequestParam @Pattern(regexp = COORDINATE, message = "origin 坐标格式错误，应形如 116.397428,39.909230") String origin,
            @Parameter(description = "终点坐标（经度,纬度）", example = "116.403963,39.915119", required = true)
            @RequestParam @Pattern(regexp = COORDINATE, message = "destination 坐标格式错误") String destination)
            throws IOException {
        return amapClient.drivingRoute(origin, destination);
    }

    @Operation(summary = "步行路径规划",
            description = "短距离步行导航。起终点直线距离建议 < 100km。")
    @GetMapping("/route/walking")
    public Route walkingRoute(
            @Parameter(description = "起点坐标（经度,纬度）", example = "116.397428,39.909230", required = true)
            @RequestParam @Pattern(regexp = COORDINATE, message = "origin 坐标格式错误") String origin,
            @Parameter(description = "终点坐标（经度,纬度）", example = "116.403963,39.915119", required = true)
            @RequestParam @Pattern(regexp = COORDINATE, message = "destination 坐标格式错误") String destination)
            throws IOException {
        return amapClient.walkingRoute(origin, destination);
    }

    @Operation(summary = "骑行路径规划",
            description = "骑行导航，使用高德 v4 接口。返回结构与驾车兼容。")
    @GetMapping("/route/bicycling")
    public Route bicyclingRoute(
            @Parameter(description = "起点坐标（经度,纬度）", example = "116.397428,39.909230", required = true)
            @RequestParam @Pattern(regexp = COORDINATE, message = "origin 坐标格式错误") String origin,
            @Parameter(description = "终点坐标（经度,纬度）", example = "116.403963,39.915119", required = true)
            @RequestParam @Pattern(regexp = COORDINATE, message = "destination 坐标格式错误") String destination)
            throws IOException {
        return amapClient.bicyclingRoute(origin, destination);
    }

    @Operation(summary = "公交路径规划（含地铁）",
            description = "公交/地铁换乘方案查询。同城只需填 city；跨城（如北京→上海）须同时填 city + cityd。")
    @GetMapping("/route/transit")
    public TransitRoute transitRoute(
            @Parameter(description = "起点坐标（经度,纬度）", example = "116.481499,39.989394", required = true)
            @RequestParam @Pattern(regexp = COORDINATE, message = "origin 坐标格式错误") String origin,
            @Parameter(description = "终点坐标（经度,纬度）", example = "116.434446,39.90816", required = true)
            @RequestParam @Pattern(regexp = COORDINATE, message = "destination 坐标格式错误") String destination,
            @Parameter(description = "起点城市名或 adcode（必填）", example = "北京", required = true)
            @RequestParam @NotBlank(message = "city 不能为空") String city,
            @Parameter(description = "终点城市名或 adcode（跨城必填）", example = "上海")
            @RequestParam(required = false) String cityd) throws IOException {
        return amapClient.transitRoute(origin, destination, city, cityd);
    }

    // =====================================================================
    // 4. 地理编码 / 逆地理编码
    // =====================================================================

    @Operation(summary = "地理编码（地址 → 坐标）",
            description = "结构化地址转经纬度。一个地址可能返回多个候选结果。")
    @GetMapping("/geocode")
    public List<Geocode> geocode(
            @Parameter(description = "结构化地址", example = "上海市徐汇区肇嘉浜路1065号", required = true)
            @RequestParam @NotBlank(message = "address 不能为空") String address,
            @Parameter(description = "指定城市（提高精度），可选", example = "上海")
            @RequestParam(required = false) String city) throws IOException {
        return amapClient.geocode(address, city);
    }

    @Operation(summary = "逆地理编码（坐标 → 地址）",
            description = "经纬度转规整化地址，包含省市区街道等信息。")
    @GetMapping("/regeocode")
    public ReGeocode regeocode(
            @Parameter(description = "坐标（经度,纬度）", example = "116.481028,39.989643", required = true)
            @RequestParam @Pattern(regexp = COORDINATE, message = "location 坐标格式错误") String location)
            throws IOException {
        return amapClient.regeocode(location);
    }
}
