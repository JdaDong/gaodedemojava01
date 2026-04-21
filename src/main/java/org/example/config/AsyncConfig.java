package org.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行线程池配置（T15）。
 * <p>
 * 提供名为 {@code amapAsyncExecutor} 的线程池，供 {@link org.springframework.scheduling.annotation.Async}
 * 注解使用。线程池尺寸针对 IO 密集型（HTTP 调用）调优。
 *
 * <p>用法：
 * <pre>
 *   {@literal @}Async("amapAsyncExecutor")
 *   public CompletableFuture&lt;List&lt;Poi&gt;&gt; searchPoiAsync(...) { ... }
 * </pre>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    public static final String EXECUTOR = "amapAsyncExecutor";

    @Bean(name = EXECUTOR)
    public Executor amapAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);            // 核心线程数
        executor.setMaxPoolSize(32);            // 最大线程数
        executor.setQueueCapacity(200);         // 任务队列容量
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("amap-async-");
        // 队列满时调用方自己执行，避免丢任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        log.info("[AsyncConfig] \u7ebf\u7a0b\u6c60\u521d\u59cb\u5316: core=8, max=32, queue=200");
        return executor;
    }
}
