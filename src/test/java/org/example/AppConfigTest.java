package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AppConfig} 单元测试
 * <p>
 * 注意：{@link AppConfig} 是单例，在测试 JVM 中只加载一次 {@code config.properties}。
 * 这里只测试 {@code System.getProperty} 可覆盖的分支，避免污染文件系统。
 */
class AppConfigTest {

    @Test
    @DisplayName("JVM 系统属性可覆盖配置文件（优先级 > properties）")
    void systemPropertyOverride() {
        String key = "amap.test.override.key";
        System.setProperty(key, "from-system-property");
        try {
            String value = AppConfig.getInstance().getString(key);
            assertEquals("from-system-property", value);
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    @DisplayName("getRequired：配置不存在 → 抛 IllegalStateException，含提示信息")
    void getRequired_missing_throws() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> AppConfig.getInstance().getRequired("this.key.definitely.does.not.exist"));

        assertTrue(ex.getMessage().contains("缺少必填配置"));
        assertTrue(ex.getMessage().contains("this.key.definitely.does.not.exist"));
    }

    @Test
    @DisplayName("getString：未配置返回 null（非必填场景）")
    void getString_notExists_returnsNull() {
        assertEquals(null, AppConfig.getInstance().getString("this.key.also.does.not.exist"));
    }

    @Test
    @DisplayName("amap.key 在 config.properties 中已填，应能被读到且非占位符")
    void getAmapKey_fromConfigProperties() {
        // 这个用例依赖本地 config.properties 存在（CI 可通过 -Damap.key=xxx 覆盖）
        String key = AppConfig.getInstance().getAmapKey();
        assertNotNull(key);
        assertTrue(key.length() >= 8, "Key 长度应 >= 8");
    }
}
