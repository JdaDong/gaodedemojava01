package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 逆地理编码结果（经纬度 → 地址）
 * <p>
 * 对应高德 `/v3/geocode/regeo` 返回的 regeocode 对象。
 * 文档：https://lbs.amap.com/api/webservice/guide/api/georegeo
 */
public class ReGeocode {

    /** 结构化地址描述，如 "北京市东城区东华门街道天安门" */
    @JSONField(name = "formatted_address")
    private String formattedAddress;

    /** 地址组件 */
    @JSONField(name = "addressComponent")
    private AddressComponent addressComponent;

    // ---------------- Getter / Setter ----------------

    public String getFormattedAddress() { return formattedAddress; }
    public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }

    public AddressComponent getAddressComponent() { return addressComponent; }
    public void setAddressComponent(AddressComponent addressComponent) { this.addressComponent = addressComponent; }

    /** 便捷获取 adcode（可用于天气查询） */
    public String getAdcode() {
        return addressComponent == null ? "" : addressComponent.getAdcodeStr();
    }

    @Override
    public String toString() {
        return "ReGeocode{" + formattedAddress + " adcode=" + getAdcode() + "}";
    }
}
