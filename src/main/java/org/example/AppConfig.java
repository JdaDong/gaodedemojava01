package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 应用配置加载器（单例）
 * <p>
 * 加载优先级（从高到低）：
 * <ol>
 *     <li>环境变量：如 {@code AMAP_KEY}（适合生产/CI 环境）</li>
 *     <li>JVM 系统属性：如 {@code -Damap.key=xxx}</li>
 *     <li>classpath 下的 {@code config.properties}</li>
 * </ol>
 * <p>
 * 如果以上都没有，则抛出 {@link IllegalStateException}，提醒用户去配置。
 */
public final class AppConfig {

    private static final String CONFIG_FILE = "config.properties";

    /** 单例实例（懒加载，线程安全：classloading 保证） */
    private static final AppConfig INSTANCE = new AppConfig();

    private final Properties properties = new Properties();

    private AppConfig() {
        loadFromClasspath();
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    /** 从 classpath 加载 config.properties；文件不存在不算错误（用户可能只用环境变量） */
    private void loadFromClasspath() {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取 " + CONFIG_FILE + " 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 按优先级读取字符串配置：
     * 环境变量（大写+下划线） > JVM 系统属性 > properties 文件。
     *
     * @param key 配置键，形如 {@code amap.key}
     * @return 配置值，未找到返回 null
     */
    public String getString(String key) {
        // 1. 环境变量：amap.key -> AMAP_KEY
        String envKey = key.toUpperCase().replace('.', '_');
        String value = System.getenv(envKey);
        if (isNotBlank(value)) return value;

        // 2. JVM 系统属性
        value = System.getProperty(key);
        if (isNotBlank(value)) return value;

        // 3. classpath 配置文件
        value = properties.getProperty(key);
        return isNotBlank(value) ? value : null;
    }

    /** 读取配置，不存在时抛异常（适合必填项） */
    public String getRequired(String key) {
        String value = getString(key);
        if (!isNotBlank(value)) {
            throw new IllegalStateException(
                    "缺少必填配置: " + key + "。请在 src/main/resources/" + CONFIG_FILE
                            + " 中填写，或通过环境变量 " + key.toUpperCase().replace('.', '_') + " 提供。");
        }
        return value;
    }

    /** 便捷方法：获取高德 Key */
    public String getAmapKey() {
        String key = getRequired("amap.key");
        // 防止模板占位符被当成真 Key 使用
        if (key.startsWith("your-") || "AMAP_KEY".equals(key)) {
            throw new IllegalStateException(
                    "amap.key 看起来仍是占位符（" + key + "），请填写真实 Key。");
        }
        return key;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
