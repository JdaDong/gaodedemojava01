package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 逆地理编码中的地址组成部分（ReGeocode.addressComponent）
 * <p>
 * 注意：高德返回的 city、district 在某些情况下可能是 "[]"（空数组而非字符串），
 * 这里先用 Object 接收，需要时再通过 {@link #asString(Object)} 转换。
 */
public class AddressComponent {

    /** 省份 */
    private Object province;

    /** 城市（直辖市可能为空数组） */
    private Object city;

    /** 城市编码 */
    private Object citycode;

    /** 区县 */
    private Object district;

    /** 区域编码 */
    private Object adcode;

    /** 乡镇/街道 */
    private Object township;

    /** 街道门牌信息 */
    @JSONField(name = "streetNumber")
    private Object streetNumber;

    // ---------------- Getter / Setter ----------------

    public Object getProvince() { return province; }
    public void setProvince(Object province) { this.province = province; }

    public Object getCity() { return city; }
    public void setCity(Object city) { this.city = city; }

    public Object getCitycode() { return citycode; }
    public void setCitycode(Object citycode) { this.citycode = citycode; }

    public Object getDistrict() { return district; }
    public void setDistrict(Object district) { this.district = district; }

    public Object getAdcode() { return adcode; }
    public void setAdcode(Object adcode) { this.adcode = adcode; }

    public Object getTownship() { return township; }
    public void setTownship(Object township) { this.township = township; }

    public Object getStreetNumber() { return streetNumber; }
    public void setStreetNumber(Object streetNumber) { this.streetNumber = streetNumber; }

    // ---------------- 便捷访问（字符串形式） ----------------

    public String getProvinceStr()  { return asString(province); }
    public String getCityStr()      { return asString(city); }
    public String getDistrictStr()  { return asString(district); }
    public String getAdcodeStr()    { return asString(adcode); }
    public String getTownshipStr()  { return asString(township); }

    /** 将字段安全转换为 String：空数组 / null 都返回 ""。 */
    private static String asString(Object o) {
        if (o == null) return "";
        String s = o.toString();
        return "[]".equals(s) ? "" : s;
    }

    @Override
    public String toString() {
        return "AddressComponent{" + getProvinceStr() + "/" + getCityStr()
                + "/" + getDistrictStr() + "/" + getTownshipStr()
                + " adcode=" + getAdcodeStr() + "}";
    }
}
