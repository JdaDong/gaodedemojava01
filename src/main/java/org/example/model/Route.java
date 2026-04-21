package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;

/**
 * 驾车路径规划结果
 * <p>
 * 对应高德 `/v3/direction/driving` 返回的 route 对象。
 */
public class Route {

    /** 起点经纬度 "lon,lat" */
    private String origin;

    /** 终点经纬度 "lon,lat" */
    private String destination;

    /** 出租车费用（元），仅当 strategy 为 10 时有效 */
    @JSONField(name = "taxi_cost")
    private String taxiCost;

    /** 推荐路线列表 */
    private List<Path> paths;

    // ---------------- Getter / Setter ----------------

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getTaxiCost() { return taxiCost; }
    public void setTaxiCost(String taxiCost) { this.taxiCost = taxiCost; }

    public List<Path> getPaths() { return paths; }
    public void setPaths(List<Path> paths) { this.paths = paths; }

    @Override
    public String toString() {
        return "Route{origin='" + origin + "' -> '" + destination + "', paths="
                + (paths == null ? 0 : paths.size()) + "}";
    }
}
