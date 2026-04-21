package org.example;

import org.example.config.AsyncConfig;
import org.example.model.Geocode;
import org.example.model.Poi;
import org.example.model.ReGeocode;
import org.example.model.Route;
import org.example.model.WeatherLive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步版 {@link AmapClient}（T15）。
 * <p>
 * <b>为什么要单独建类？</b>Spring 的 {@code @Async} 基于动态代理实现，
 * 同类内部方法互相调用会绕过代理、直接走本地方法，异步注解失效。
 * 因此异步包装必须放到独立 Bean 里，通过注入方式调用 {@link AmapClient}，
 * 才能保证代理链生效。
 *
 * <p>所有方法返回 {@link CompletableFuture}，调用方可用 {@code .thenCombine()}
 * 或 {@link CompletableFuture#allOf} 并行组合多个请求，显著降低总耗时。
 */
@Service
public class AsyncAmapClient {

    private static final Logger log = LoggerFactory.getLogger(AsyncAmapClient.class);

    private final AmapClient amapClient;

    public AsyncAmapClient(AmapClient amapClient) {
        this.amapClient = amapClient;
    }

    @Async(AsyncConfig.EXECUTOR)
    public CompletableFuture<List<Poi>> searchPoiAsync(String keywords, String city) {
        return wrap("searchPoi", () -> amapClient.searchPoi(keywords, city));
    }

    @Async(AsyncConfig.EXECUTOR)
    public CompletableFuture<WeatherLive> queryWeatherLiveAsync(String adcode) {
        return wrap("queryWeatherLive", () -> amapClient.queryWeatherLive(adcode));
    }

    @Async(AsyncConfig.EXECUTOR)
    public CompletableFuture<Route> drivingRouteAsync(String origin, String destination) {
        return wrap("drivingRoute", () -> amapClient.drivingRoute(origin, destination));
    }

    @Async(AsyncConfig.EXECUTOR)
    public CompletableFuture<List<Geocode>> geocodeAsync(String address, String city) {
        return wrap("geocode", () -> amapClient.geocode(address, city));
    }

    @Async(AsyncConfig.EXECUTOR)
    public CompletableFuture<ReGeocode> regeocodeAsync(String location) {
        return wrap("regeocode", () -> amapClient.regeocode(location));
    }

    /** 统一异常转换：把 IOException 包成 CompletionException，便于 CompletableFuture 链上处理 */
    private <T> CompletableFuture<T> wrap(String op, IoSupplier<T> task) {
        long start = System.currentTimeMillis();
        try {
            T result = task.get();
            log.debug("[async] {} \u8017\u65f6 {}ms", op, System.currentTimeMillis() - start);
            return CompletableFuture.completedFuture(result);
        } catch (IOException e) {
            log.warn("[async] {} \u5931\u8d25: {}", op, e.getMessage());
            return CompletableFuture.failedFuture(e);
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @FunctionalInterface
    private interface IoSupplier<T> {
        T get() throws IOException;
    }
}
