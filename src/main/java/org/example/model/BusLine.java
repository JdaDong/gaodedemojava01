package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 公交线路信息（Segment.BusSegment.buslines 数组中的元素）
 */
public class BusLine {

    /** 公交线路名称，如 "地铁1号线(富锦路--莘庄)" */
    private String name;

    /** 公交类型，如 "地铁线路"、"普通公交线路" */
    private String type;

    /** 上车站名 */
    @JSONField(name = "departure_stop")
    private Stop departureStop;

    /** 下车站名 */
    @JSONField(name = "arrival_stop")
    private Stop arrivalStop;

    /** 本段乘车距离（米） */
    private String distance;

    /** 本段乘车耗时（秒） */
    private String duration;

    /** 经过站点数 */
    @JSONField(name = "via_num")
    private String viaNum;

    /** 本段票价（元），可能为 "0" */
    @JSONField(name = "cost")
    private String cost;

    /** 首班车时间，格式 "HHmm" */
    @JSONField(name = "start_time")
    private String startTime;

    /** 末班车时间 */
    @JSONField(name = "end_time")
    private String endTime;

    // ---------------- Getter / Setter ----------------

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Stop getDepartureStop() { return departureStop; }
    public void setDepartureStop(Stop departureStop) { this.departureStop = departureStop; }

    public Stop getArrivalStop() { return arrivalStop; }
    public void setArrivalStop(Stop arrivalStop) { this.arrivalStop = arrivalStop; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getViaNum() { return viaNum; }
    public void setViaNum(String viaNum) { this.viaNum = viaNum; }

    public String getCost() { return cost; }
    public void setCost(String cost) { this.cost = cost; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return "BusLine{" + name + ", " + (departureStop == null ? "?" : departureStop.getName())
                + " -> " + (arrivalStop == null ? "?" : arrivalStop.getName()) + "}";
    }

    // =====================================================================
    // 内嵌：站点
    // =====================================================================

    public static class Stop {
        private String name;
        private String id;
        private String location;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        @Override
        public String toString() { return name + "(" + location + ")"; }
    }
}
