package org.example;

import com.alibaba.fastjson2.JSONObject;

import java.util.function.Function;

/**
 * 高德 API 统一响应封装
 * <p>
 * 高德所有接口都会返回固定的元数据字段：
 * <pre>
 * {
 *   "status":   "1",       // "1"=成功, "0"=失败
 *   "info":     "OK",      // 错误描述
 *   "infocode": "10000",   // 错误码
 *   "count":    "1",
 *   ...具体业务数据...
 * }
 * </pre>
 * 本类将这些字段统一封装，并提供 {@link #parse(JSONObject, String, Function)} 方法
 * 自动完成「状态校验 + 业务数据提取 + 失败抛 {@link AmapException}」三步。
 *
 * @param <T> 业务数据类型
 */
public class AmapResponse<T> {

    /** 成功状态码 */
    public static final String STATUS_OK = "1";

    private final String status;
    private final String info;
    private final String infoCode;
    private final String count;
    private final T data;

    private AmapResponse(String status, String info, String infoCode, String count, T data) {
        this.status = status;
        this.info = info;
        this.infoCode = infoCode;
        this.count = count;
        this.data = data;
    }

    // ---------------- Getter ----------------

    public String getStatus()   { return status; }
    public String getInfo()     { return info; }
    public String getInfoCode() { return infoCode; }
    public String getCount()    { return count; }
    public T      getData()     { return data; }

    public boolean isSuccess() { return STATUS_OK.equals(status); }

    // ---------------- 统一解析入口 ----------------

    /**
     * 解析原始 JSON 并提取业务数据。<br>
     * 失败（status != "1" 或 json 为 null）时抛出 {@link AmapException}。
     *
     * @param json       原始响应 JSON
     * @param apiName    API 中文名（用于异常提示），如 "POI 搜索"
     * @param dataParser 从 json 提取业务数据的函数（通常是 {@code j -> j.getJSONArray("pois").toJavaList(Poi.class)} 等）
     * @return 封装后的 AmapResponse，调用 {@link #getData()} 获取数据
     */
    public static <T> AmapResponse<T> parse(JSONObject json,
                                            String apiName,
                                            Function<JSONObject, T> dataParser) {
        if (json == null) {
            throw new AmapException(apiName, "EMPTY_RESPONSE", "响应为空");
        }
        String status   = json.getString("status");
        String info     = json.getString("info");
        String infoCode = json.getString("infocode");
        String count    = json.getString("count");

        if (!STATUS_OK.equals(status)) {
            throw new AmapException(apiName, infoCode, info);
        }
        T data = dataParser == null ? null : dataParser.apply(json);
        return new AmapResponse<>(status, info, infoCode, count, data);
    }

    @Override
    public String toString() {
        return "AmapResponse{status=" + status + ", info=" + info
                + ", infocode=" + infoCode + ", count=" + count + "}";
    }
}
