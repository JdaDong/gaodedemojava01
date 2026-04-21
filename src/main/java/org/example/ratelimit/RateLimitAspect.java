package org.example.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link RateLimit} 注解的 AOP 实现。
 * <p>
 * 为每个被标注的方法维护独立的令牌桶；{@link RateLimit.Scope#PER_IP} 时按
 * <code>方法签名 + 客户端 IP</code> 为粒度构建多个桶。
 */
@Aspect
@Component
public class RateLimitAspect {

    /** 每个限流点对应的令牌桶。key：方法全名（GLOBAL）或 方法全名+IP（PER_IP） */
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Around("@annotation(org.example.ratelimit.RateLimit)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        RateLimit anno = method.getAnnotation(RateLimit.class);

        String key = buildKey(method, anno.scope());
        TokenBucket bucket = buckets.computeIfAbsent(key,
                k -> new TokenBucket(anno.qps(), anno.burst()));

        if (!bucket.tryAcquire()) {
            throw new RateLimitException("\u63a5\u53e3 [" + method.getName()
                    + "] \u8bf7\u6c42\u8fc7\u4e8e\u9891\u7e41\uff08\u9650\u6d41\uff1aQPS="
                    + anno.qps() + "\uff0cburst=" + anno.burst() + "\uff09");
        }
        return pjp.proceed();
    }

    private String buildKey(Method method, RateLimit.Scope scope) {
        String base = method.getDeclaringClass().getName() + "#" + method.getName();
        if (scope == RateLimit.Scope.PER_IP) {
            return base + "@" + currentClientIp();
        }
        return base;
    }

    /** 从当前请求中取客户端 IP，优先 X-Forwarded-For 第一跳 */
    private String currentClientIp() {
        try {
            ServletRequestAttributes attr =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest req = attr.getRequest();
            String xff = req.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                int comma = xff.indexOf(',');
                return (comma > 0 ? xff.substring(0, comma) : xff).trim();
            }
            return req.getRemoteAddr();
        } catch (IllegalStateException e) {
            // 非 Web 请求场景（如定时任务调用了被限流的方法），退化为 GLOBAL
            return "NON_WEB";
        }
    }
}
