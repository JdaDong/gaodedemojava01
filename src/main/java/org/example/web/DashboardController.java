package org.example.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.example.AsyncAmapClient;
import org.example.model.Poi;
import org.example.model.Route;
import org.example.model.WeatherLive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步组合接口（T15 演示）。
 * <p>
 * {@code /api/dashboard} 同时查询：POI + 实时天气 + 驾车路径，使用 {@link CompletableFuture}
 * 并行发起 3 个 HTTP 请求。
 * <p>
 * 对比串行：如果串行调用，总耗时约 = 请求1 + 请求2 + 请求3（约 600ms）；
 * 并行后，总耗时 ≈ max(请求1, 请求2, 请求3)（约 200ms），速度提升约 3 倍。
 */
@Tag(name = "\u7efc\u5408\u9762\u677f\uff08\u5f02\u6b65\uff09", description = "\u4e00\u6b21\u8bf7\u6c42\u5e76\u884c\u83b7\u53d6 POI+\u5929\u6c14+\u8def\u7ebf\uff08T15\uff09")
@Validated
@RestController
@RequestMapping("/api")
public class DashboardController {

    private static final String COORDINATE =
            "^-?((1[0-7]\\d)|(\\d{1,2}))(\\.\\d{1,6})?,-?((90)|([1-8]?\\d))(\\.\\d{1,6})?$";
    private static final String ADCODE = "^\\d{6}$";

    private final AsyncAmapClient asyncClient;

    public DashboardController(AsyncAmapClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    @Operation(summary = "\u7efc\u5408\u4eea\u8868\u76d8\uff08\u5e76\u884c\u8c03\u7528\uff09",
            description = "\u540c\u65f6\u62c9\u53d6 POI\u3001\u5b9e\u65f6\u5929\u6c14\u3001\u9a7e\u8f66\u8def\u5f84\u4e09\u4e2a\u6570\u636e\u6e90\uff0c\u8017\u65f6 = \u6700\u6162\u7684\u90a3\u4e2a\u8bf7\u6c42\u3002")
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(
            @Parameter(description = "POI \u5173\u952e\u5b57", example = "\u80af\u5fb7\u57fa", required = true)
            @RequestParam @NotBlank String keywords,
            @Parameter(description = "\u57ce\u5e02 adcode", example = "110000", required = true)
            @RequestParam @Pattern(regexp = ADCODE, message = "adcode \u5fc5\u987b\u662f 6 \u4f4d\u6570\u5b57") String adcode,
            @Parameter(description = "\u8def\u7ebf\u8d77\u70b9\uff08\u7ecf,\u7eac\uff09", example = "116.397428,39.909230", required = true)
            @RequestParam @Pattern(regexp = COORDINATE) String origin,
            @Parameter(description = "\u8def\u7ebf\u7ec8\u70b9\uff08\u7ecf,\u7eac\uff09", example = "116.403963,39.915119", required = true)
            @RequestParam @Pattern(regexp = COORDINATE) String destination) {

        long start = System.currentTimeMillis();

        // 并行发起 3 个请求
        CompletableFuture<List<Poi>> poiF = asyncClient.searchPoiAsync(keywords, null);
        CompletableFuture<WeatherLive> weatherF = asyncClient.queryWeatherLiveAsync(adcode);
        CompletableFuture<Route> routeF = asyncClient.drivingRouteAsync(origin, destination);

        // 等所有完成（任一失败会在 .join() 里抛异常，交给 GlobalExceptionHandler）
        CompletableFuture.allOf(poiF, weatherF, routeF).join();

        Map<String, Object> body = new LinkedHashMap<>();
        // 每个子任务用 getNow 兜底，避免任何一个意外阻塞
        body.put("poi", poiF.join());
        body.put("weather", weatherF.join());
        body.put("route", routeF.join());
        body.put("elapsedMs", System.currentTimeMillis() - start);
        return body;
    }
}
