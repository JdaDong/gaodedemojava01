package org.example.ratelimit;

/**
 * 限流触发时抛出的异常，由 {@link org.example.web.GlobalExceptionHandler} 转为 HTTP 429。
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
