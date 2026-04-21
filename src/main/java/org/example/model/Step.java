package org.example.model;

/**
 * 驾车路线中的一步行动指示（Path.steps 数组中的元素）
 */
public class Step {

    /** 行驶指示，如 "沿长安街向东行驶 500 米" */
    private String instruction;

    /** 道路名称 */
    private String road;

    /** 此段距离（米） */
    private String distance;

    /** 此段预计耗时（秒） */
    private String duration;

    /** 此段轨迹坐标串 */
    private String polyline;

    /** 动作，如 "直行"、"左转" */
    private String action;

    /** 辅助动作，如 "到达目的地" */
    private String assistant_action;

    /** 方向，步行/骑行特有，如 "东"、"东南" */
    private String orientation;

    // ---------------- Getter / Setter ----------------

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }

    public String getRoad() { return road; }
    public void setRoad(String road) { this.road = road; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getPolyline() { return polyline; }
    public void setPolyline(String polyline) { this.polyline = polyline; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getAssistant_action() { return assistant_action; }
    public void setAssistant_action(String assistant_action) { this.assistant_action = assistant_action; }

    public String getOrientation() { return orientation; }
    public void setOrientation(String orientation) { this.orientation = orientation; }

    @Override
    public String toString() {
        return "Step{" + instruction + " [" + distance + "m / " + duration + "s]}";
    }
}
