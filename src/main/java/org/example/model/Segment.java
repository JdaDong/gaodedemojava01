package org.example.model;

import java.util.List;

/**
 * 公交方案中的一段（Transit.segments 数组中的元素）。
 * <p>
 * 每段可能包含一段步行 {@link #walking} + 一段乘车 {@link #bus}。
 * 也可能只有其中之一。
 */
public class Segment {

    /** 这一段的步行部分（从当前站点到下一换乘站点） */
    private WalkingSegment walking;

    /** 这一段的乘车部分（可能有多条可选公交线路） */
    private BusSegment bus;

    // ---------------- Getter / Setter ----------------

    public WalkingSegment getWalking() { return walking; }
    public void setWalking(WalkingSegment walking) { this.walking = walking; }

    public BusSegment getBus() { return bus; }
    public void setBus(BusSegment bus) { this.bus = bus; }

    // =====================================================================
    // 内嵌类：步行段
    // =====================================================================

    public static class WalkingSegment {
        private String origin;
        private String destination;
        private String distance;
        private String duration;
        private List<Step> steps;

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public String getDistance() { return distance; }
        public void setDistance(String distance) { this.distance = distance; }

        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }

        public List<Step> getSteps() { return steps; }
        public void setSteps(List<Step> steps) { this.steps = steps; }

        @Override
        public String toString() {
            return "Walking{" + distance + "m / " + duration + "s}";
        }
    }

    // =====================================================================
    // 内嵌类：乘车段（可能有多条并行可选的公交线路）
    // =====================================================================

    public static class BusSegment {
        /** 候选公交线路（同站点可能有多条公交都能坐） */
        private List<BusLine> buslines;

        public List<BusLine> getBuslines() { return buslines; }
        public void setBuslines(List<BusLine> buslines) { this.buslines = buslines; }

        @Override
        public String toString() {
            return "Bus{lines=" + (buslines == null ? 0 : buslines.size()) + "}";
        }
    }
}
