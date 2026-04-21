package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 高德相关配置（绑定 {@code amap.*} 前缀）。
 * <p>
 * 配置来源（按 Spring Boot 的 PropertySource 优先级从高到低）：
 * <ol>
 *     <li>命令行参数：{@code --amap.key=xxx}</li>
 *     <li>JVM 系统属性：{@code -Damap.key=xxx}</li>
 *     <li>环境变量：{@code AMAP_KEY}（Spring Boot 会自动做 {@code amap.key} ↔ {@code AMAP_KEY} 的松绑定）</li>
 *     <li>{@code application.yml / application.properties}</li>
 * </ol>
 *
 * @param key         高德 Web 服务 Key（必填）
 * @param baseUrl     API 基地址，默认为官方地址；测试或私有网关场景可覆盖
 * @param connectTimeoutSeconds 连接超时（秒）
 * @param readTimeoutSeconds    读取超时（秒）
 */
@ConfigurationProperties(prefix = "amap")
public record AmapProperties(
        String key,
        String baseUrl,
        Integer connectTimeoutSeconds,
        Integer readTimeoutSeconds
) {
    /** 官方默认地址 */
    public static final String DEFAULT_BASE_URL = "https://restapi.amap.com/v3";

    /** 紧凑构造器：应用默认值，避免 NPE */
    public AmapProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        if (connectTimeoutSeconds == null || connectTimeoutSeconds <= 0) {
            connectTimeoutSeconds = 5;
        }
        if (readTimeoutSeconds == null || readTimeoutSeconds <= 0) {
            readTimeoutSeconds = 10;
        }
    }
}
