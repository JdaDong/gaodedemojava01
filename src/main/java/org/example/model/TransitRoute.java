package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;

/**
 * 公交路径规划的总响应结构
 * <p>
 * 对应高德 {@code /v3/direction/transit/integrated} 返回中的 {@code route} 对象。
 * 与驾车的 {@link Route} 不同，公交返回的是多个"换乘方案" {@link Transit}。
 */
public class TransitRoute {

    /** 起点经纬度 "lon,lat" */
    private String origin;

    /** 终点经纬度 "lon,lat" */
    private String destination;

    /** 起终点之间的直线距离（米） */
    private String distance;

    /** 出租车大约费用（元） */
    @JSONField(name = "taxi_cost")
    private String taxiCost;

    /** 换乘方案列表（多条公交线路方案） */
    private List<Transit> transits;

    // ---------------- Getter / Setter ----------------

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public String getTaxiCost() { return taxiCost; }
    public void setTaxiCost(String taxiCost) { this.taxiCost = taxiCost; }

    public List<Transit> getTransits() { return transits; }
    public void setTransits(List<Transit> transits) { this.transits = transits; }

    @Override
    public String toString() {
        return "TransitRoute{origin='" + origin + "' -> '" + destination + "', transits="
                + (transits == null ? 0 : transits.size()) + "}";
    }
}
