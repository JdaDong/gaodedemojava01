package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * POI 搜索结果项
 * <p>
 * 对应高德 `/v3/place/text` 接口返回的 pois 数组中每一项。
 * 文档：https://lbs.amap.com/api/webservice/guide/api/search
 */
public class Poi {

    /** POI 唯一 ID */
    private String id;

    /** POI 名称，如 "肯德基(和平店)" */
    private String name;

    /** POI 类型，如 "餐饮服务;快餐厅;肯德基" */
    private String type;

    /** 详细地址 */
    private String address;

    /** 经纬度，格式 "经度,纬度" */
    private String location;

    /** 电话（可能为空或数组，这里按字符串接收） */
    private String tel;

    /** 省份名 */
    @JSONField(name = "pname")
    private String provinceName;

    /** 城市名 */
    @JSONField(name = "cityname")
    private String cityName;

    /** 区县名 */
    @JSONField(name = "adname")
    private String districtName;

    // ---------------- Getter / Setter ----------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    @Override
    public String toString() {
        return "Poi{name='" + name + "', address='" + address + "', location='" + location + "'}";
    }
}
