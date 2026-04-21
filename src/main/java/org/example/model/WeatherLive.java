package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 实时天气（extensions=base 时 lives 数组中的元素）
 * <p>
 * 文档：https://lbs.amap.com/api/webservice/guide/api/weatherinfo
 */
public class WeatherLive {

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 城市编码 */
    private String adcode;

    /** 天气现象，如 "晴"、"多云" */
    private String weather;

    /** 实时温度（℃） */
    private String temperature;

    /** 风向 */
    @JSONField(name = "winddirection")
    private String windDirection;

    /** 风力（级） */
    @JSONField(name = "windpower")
    private String windPower;

    /** 湿度（%） */
    private String humidity;

    /** 数据发布时间，格式 yyyy-MM-dd HH:mm:ss */
    @JSONField(name = "reporttime")
    private String reportTime;

    // ---------------- Getter / Setter ----------------

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAdcode() { return adcode; }
    public void setAdcode(String adcode) { this.adcode = adcode; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }

    public String getWindDirection() { return windDirection; }
    public void setWindDirection(String windDirection) { this.windDirection = windDirection; }

    public String getWindPower() { return windPower; }
    public void setWindPower(String windPower) { this.windPower = windPower; }

    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }

    public String getReportTime() { return reportTime; }
    public void setReportTime(String reportTime) { this.reportTime = reportTime; }

    @Override
    public String toString() {
        return "WeatherLive{city='" + city + "', weather='" + weather
                + "', temperature='" + temperature + "℃', windDirection='" + windDirection + "'}";
    }
}
