package org.example.model;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;

/**
 * 一条公交换乘方案（TransitRoute.transits 数组中的元素）
 */
public class Transit {

    /** 该方案总花费（元，包含地铁、公交等票价） */
    private String cost;

    /** 该方案预计耗时（秒） */
    private String duration;

    /** 是否夜班车 "0"=否 "1"=是 */
    private String nightflag;

    /** 总步行距离（米） */
    @JSONField(name = "walking_distance")
    private String walkingDistance;

    /** 总距离（米） */
    private String distance;

    /** 具体的分段列表（每段可能是步行+乘车组合） */
    private List<Segment> segments;

    // ---------------- Getter / Setter ----------------

    public String getCost() { return cost; }
    public void setCost(String cost) { this.cost = cost; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getNightflag() { return nightflag; }
    public void setNightflag(String nightflag) { this.nightflag = nightflag; }

    public String getWalkingDistance() { return walkingDistance; }
    public void setWalkingDistance(String walkingDistance) { this.walkingDistance = walkingDistance; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public List<Segment> getSegments() { return segments; }
    public void setSegments(List<Segment> segments) { this.segments = segments; }

    @Override
    public String toString() {
        return "Transit{duration=" + duration + "s, cost=" + cost + ", walking="
                + walkingDistance + "m, segments="
                + (segments == null ? 0 : segments.size()) + "}";
    }
}
