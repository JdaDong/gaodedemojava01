package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 预报中的单日数据（WeatherForecast.casts 数组中的元素）
 */
public class WeatherCast {

    /** 日期 yyyy-MM-dd */
    private String date;

    /** 星期几，1 表示周一，7 表示周日 */
    private String week;

    /** 白天天气现象 */
    @JSONField(name = "dayweather")
    private String dayWeather;

    /** 夜间天气现象 */
    @JSONField(name = "nightweather")
    private String nightWeather;

    /** 白天温度 */
    @JSONField(name = "daytemp")
    private String dayTemp;

    /** 夜间温度 */
    @JSONField(name = "nighttemp")
    private String nightTemp;

    /** 白天风向 */
    @JSONField(name = "daywind")
    private String dayWind;

    /** 夜间风向 */
    @JSONField(name = "nightwind")
    private String nightWind;

    /** 白天风力 */
    @JSONField(name = "daypower")
    private String dayPower;

    /** 夜间风力 */
    @JSONField(name = "nightpower")
    private String nightPower;

    // ---------------- Getter / Setter ----------------

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }

    public String getDayWeather() { return dayWeather; }
    public void setDayWeather(String dayWeather) { this.dayWeather = dayWeather; }

    public String getNightWeather() { return nightWeather; }
    public void setNightWeather(String nightWeather) { this.nightWeather = nightWeather; }

    public String getDayTemp() { return dayTemp; }
    public void setDayTemp(String dayTemp) { this.dayTemp = dayTemp; }

    public String getNightTemp() { return nightTemp; }
    public void setNightTemp(String nightTemp) { this.nightTemp = nightTemp; }

    public String getDayWind() { return dayWind; }
    public void setDayWind(String dayWind) { this.dayWind = dayWind; }

    public String getNightWind() { return nightWind; }
    public void setNightWind(String nightWind) { this.nightWind = nightWind; }

    public String getDayPower() { return dayPower; }
    public void setDayPower(String dayPower) { this.dayPower = dayPower; }

    public String getNightPower() { return nightPower; }
    public void setNightPower(String nightPower) { this.nightPower = nightPower; }

    @Override
    public String toString() {
        return "WeatherCast{" + date + " 白天:" + dayWeather + "/" + dayTemp + "℃"
                + "，夜间:" + nightWeather + "/" + nightTemp + "℃}";
    }
}
