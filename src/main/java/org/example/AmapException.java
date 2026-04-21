package org.example;

/**
 * 高德 API 业务异常
 * <p>
 * 当高德接口返回 {@code status != "1"} 时抛出，
 * 携带 {@code infocode / info / apiName} 便于定位问题。
 * <p>
 * 常见 infocode：
 * <ul>
 *     <li>10001 - INVALID_USER_KEY：Key 不正确或过期</li>
 *     <li>10003 - DAILY_QUERY_OVER_LIMIT：访问已超出日访问量</li>
 *     <li>10004 - ACCESS_TOO_FREQUENT：单位时间内访问过于频繁</li>
 *     <li>20003 - ENGINE_RESPONSE_DATA_ERROR：请求服务响应错误</li>
 * </ul>
 * 完整列表：https://lbs.amap.com/api/webservice/guide/tools/info
 */
public class AmapException extends RuntimeException {

    /** 高德 infocode，如 "10001" */
    private final String infoCode;

    /** 高德 info，错误的简短描述 */
    private final String info;

    /** 出错的 API 名称（中文），如 "POI 搜索"，由调用方传入 */
    private final String apiName;

    public AmapException(String apiName, String infoCode, String info) {
        super(apiName + " 调用失败: infocode=" + infoCode + ", info=" + info);
        this.apiName = apiName;
        this.infoCode = infoCode;
        this.info = info;
    }

    public AmapException(String apiName, String message, Throwable cause) {
        super(apiName + " 调用失败: " + message, cause);
        this.apiName = apiName;
        this.infoCode = null;
        this.info = message;
    }

    public String getInfoCode() { return infoCode; }

    public String getInfo() { return info; }

    public String getApiName() { return apiName; }
}
