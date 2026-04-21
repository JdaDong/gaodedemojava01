package org.example;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AmapResponse} 单元测试
 */
class AmapResponseTest {

    @Test
    @DisplayName("parse：status=1 → 成功返回封装对象，数据由 dataParser 提取")
    void parse_success() {
        JSONObject json = JSON.parseObject("""
                {"status":"1","info":"OK","infocode":"10000","count":"3","foo":"bar"}
                """);

        AmapResponse<String> resp = AmapResponse.parse(json, "Test API",
                j -> j.getString("foo"));

        assertTrue(resp.isSuccess());
        assertEquals("1", resp.getStatus());
        assertEquals("OK", resp.getInfo());
        assertEquals("10000", resp.getInfoCode());
        assertEquals("3", resp.getCount());
        assertEquals("bar", resp.getData());
    }

    @Test
    @DisplayName("parse：status=0 → 抛 AmapException，携带 infocode 和 info")
    void parse_statusNotOk_throws() {
        JSONObject json = JSON.parseObject("""
                {"status":"0","info":"INVALID USER KEY","infocode":"10001"}
                """);

        AmapException ex = assertThrows(AmapException.class,
                () -> AmapResponse.parse(json, "POI 搜索", j -> j));

        assertEquals("10001", ex.getInfoCode());
        assertEquals("INVALID USER KEY", ex.getInfo());
        assertEquals("POI 搜索", ex.getApiName());
    }

    @Test
    @DisplayName("parse：json=null → 抛 AmapException(EMPTY_RESPONSE)")
    void parse_nullJson_throws() {
        AmapException ex = assertThrows(AmapException.class,
                () -> AmapResponse.parse(null, "Test API", j -> j));

        assertEquals("EMPTY_RESPONSE", ex.getInfoCode());
    }

    @Test
    @DisplayName("parse：dataParser 为 null 时 data 字段应为 null，不抛异常")
    void parse_nullDataParser() {
        JSONObject json = JSON.parseObject("""
                {"status":"1","info":"OK","infocode":"10000"}
                """);

        AmapResponse<Object> resp = AmapResponse.parse(json, "Test", null);

        assertNotNull(resp);
        assertTrue(resp.isSuccess());
        assertEquals(null, resp.getData());
    }
}
