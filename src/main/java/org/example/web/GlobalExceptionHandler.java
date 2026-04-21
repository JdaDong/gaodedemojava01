package org.example.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.example.AmapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常处理：将业务/IO 异常统一转成结构化 JSON 响应，不把堆栈直接回显给调用方。
 * <p>
 * 响应结构：
 * <pre>
 * {
 *   "timestamp":  "2026-04-21T10:15:30+08:00",
 *   "status":     400,
 *   "error":      "AMAP_API_ERROR",
 *   "infocode":   "10001",
 *   "message":    "INVALID USER KEY",
 *   "api":        "POI 搜索"
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 高德业务异常：status=0 / infocode 非 10000 */
    @ExceptionHandler(AmapException.class)
    public ResponseEntity<Map<String, Object>> handleAmap(AmapException ex) {
        log.warn("[Amap 业务异常] api={} infocode={} info={}",
                ex.getApiName(), ex.getInfoCode(), ex.getInfo());
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "AMAP_API_ERROR", ex.getInfo());
        body.put("infocode", ex.getInfoCode());
        body.put("api", ex.getApiName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** 网络/IO 异常：高德服务器或网络层面的问题 */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIO(IOException ex) {
        log.error("[IO 异常] {}", ex.getMessage(), ex);
        Map<String, Object> body = baseBody(HttpStatus.BAD_GATEWAY, "UPSTREAM_IO_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    /** 缺少必填参数：比如 /api/poi 没传 keywords */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                "缺少请求参数：" + ex.getParameterName());
        return ResponseEntity.badRequest().body(body);
    }

    /** Bean Validation 失败（@RequestParam 上的 @NotBlank、@Pattern 等注解触发） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ConstraintViolationException ex) {
        List<Map<String, String>> violations = new ArrayList<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            Map<String, String> v = new LinkedHashMap<>();
            // propertyPath 形如 "searchPoi.keywords"，取最后一段就是字段名
            String path = cv.getPropertyPath().toString();
            v.put("field", path.substring(path.lastIndexOf('.') + 1));
            v.put("message", cv.getMessage());
            v.put("rejected", String.valueOf(cv.getInvalidValue()));
            violations.add(v);
        }
        log.warn("[\u53c2\u6570\u6821\u9a8c\u5931\u8d25] {}", violations);
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "\u8bf7\u6c42\u53c2\u6570\u6821\u9a8c\u5931\u8d25");
        body.put("violations", violations);
        return ResponseEntity.badRequest().body(body);
    }

    /** 参数类型不匹配：比如给期望 int 的参数传了字符串 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() == null ? "?" : ex.getRequiredType().getSimpleName();
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH",
                "\u53c2\u6570 " + ex.getName() + " \u7c7b\u578b\u9519\u8bef\uff0c\u671f\u671b " + expected);
        return ResponseEntity.badRequest().body(body);
    }

    /** 限流触发（T11） */
    @ExceptionHandler(org.example.ratelimit.RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(org.example.ratelimit.RateLimitException ex) {
        log.warn("[\u9650\u6d41] {}", ex.getMessage());
        Map<String, Object> body = baseBody(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", ex.getMessage());
        body.put("retryAfterSeconds", 1);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "1").body(body);
    }

    /** 兜底异常：避免堆栈直接回显给调用方 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
        log.error("[未捕获异常] {}", ex.getMessage(), ex);
        Map<String, Object> body = baseBody(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR", ex.getMessage());
        return ResponseEntity.internalServerError().body(body);
    }

    /** 构建基础错误体（用 LinkedHashMap 保证字段顺序稳定，便于前端调试） */
    private static Map<String, Object> baseBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
