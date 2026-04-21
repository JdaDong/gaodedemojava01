package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 地理编码结果（地址 → 经纬度）
 * <p>
 * 对应高德 `/v3/geocode/geo` 返回的 geocodes 数组中每一项。
 * 文档：https://lbs.amap.com/api/webservice/guide/api/georegeo
 */
public class Geocode {

    /** 结构化地址信息 */
    @JSONField(name = "formatted_address")
    private String formattedAddress;

    /** 省份 */
    private String province;

    /** 城市名 */
    private String city;

    /** 区县 */
    private String district;

    /** 区域编码（adcode），可用于天气查询 */
    private String adcode;

    /** 城市编码（citycode） */
    private String citycode;

    /** 经纬度，格式 "经度,纬度" */
    private String location;

    /**
     * 匹配级别，如 "门牌号"、"兴趣点"、"区县"、"省" 等。
     * 级别越细，定位越精确。
     */
    private String level;

    // ---------------- Getter / Setter ----------------

    public String getFormattedAddress() { return formattedAddress; }
    public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getAdcode() { return adcode; }
    public void setAdcode(String adcode) { this.adcode = adcode; }

    public String getCitycode() { return citycode; }
    public void setCitycode(String citycode) { this.citycode = citycode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    @Override
    public String toString() {
        return "Geocode{" + formattedAddress + " @ " + location
                + " (adcode=" + adcode + ", level=" + level + ")}";
    }
}
