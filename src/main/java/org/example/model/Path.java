package org.example.model;

import java.util.List;

/**
 * 单条驾车路线（Route.paths 数组中的元素）
 */
public class Path {

    /** 总距离（米） */
    private String distance;

    /** 总预计耗时（秒） */
    private String duration;

    /** 使用的策略编号 */
    private String strategy;

    /** 收费总额（元），可能为 "0" */
    private String tolls;

    /** 收费路段距离（米） */
    private String toll_distance;

    /** 路线详细步骤 */
    private List<Step> steps;

    // ---------------- Getter / Setter ----------------

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public String getTolls() { return tolls; }
    public void setTolls(String tolls) { this.tolls = tolls; }

    public String getToll_distance() { return toll_distance; }
    public void setToll_distance(String toll_distance) { this.toll_distance = toll_distance; }

    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }

    @Override
    public String toString() {
        return "Path{distance=" + distance + "m, duration=" + duration + "s, steps="
                + (steps == null ? 0 : steps.size()) + "}";
    }
}
