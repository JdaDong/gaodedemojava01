package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;

/**
 * 天气预报（extensions=all 时 forecasts 数组中的元素）
 */
public class WeatherForecast {

    /** 城市名 */
    private String city;

    /** 城市编码 */
    private String adcode;

    /** 省份 */
    private String province;

    /** 数据发布时间 */
    @JSONField(name = "reporttime")
    private String reportTime;

    /** 未来几天的预报列表 */
    private List<WeatherCast> casts;

    // ---------------- Getter / Setter ----------------

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAdcode() { return adcode; }
    public void setAdcode(String adcode) { this.adcode = adcode; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getReportTime() { return reportTime; }
    public void setReportTime(String reportTime) { this.reportTime = reportTime; }

    public List<WeatherCast> getCasts() { return casts; }
    public void setCasts(List<WeatherCast> casts) { this.casts = casts; }

    @Override
    public String toString() {
        return "WeatherForecast{city='" + city + "', reportTime='" + reportTime
                + "', casts=" + (casts == null ? 0 : casts.size()) + " 天}";
    }
}
