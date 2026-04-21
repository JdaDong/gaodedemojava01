package org.example.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解。加在 Controller 方法上即可启用。
 * <p>
 * 示例：
 * <pre>
 *   {@literal @}RateLimit(qps = 5, burst = 10)
 *   {@literal @}GetMapping("/poi")
 *   public List&lt;Poi&gt; searchPoi(...) { ... }
 * </pre>
 *
 * 超限时抛出 {@link RateLimitException}，由全局异常处理器转为 HTTP 429。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimit {

    /** 稳态 QPS，默认 5（高德个人 Key 免费配额） */
    double qps() default 5.0;

    /** 突发容量，默认 10 */
    int burst() default 10;

    /** 限流粒度：全局共享同一个桶，或按客户端 IP 独立桶 */
    Scope scope() default Scope.GLOBAL;

    enum Scope {
        /** 所有调用者共享同一个桶 */
        GLOBAL,
        /** 按调用方 IP 独立桶 */
        PER_IP
    }
}
