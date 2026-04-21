package org.example;

import org.example.model.Geocode;
import org.example.model.Path;
import org.example.model.Poi;
import org.example.model.ReGeocode;
import org.example.model.Route;
import org.example.model.TransitRoute;
import org.example.model.WeatherLive;

import java.util.List;

/**
 * 高德地图 API 调用示例（POJO 风格）
 */
public class Main {

    public static void main(String[] args) {
        // Key 从外部化配置读取（环境变量 AMAP_KEY > JVM -Damap.key > config.properties）
        String apiKey = AppConfig.getInstance().getAmapKey();
        AmapClient client = new AmapClient(apiKey);

        try {
            // ========== 1. POI 搜索：在上海搜索 "肯德基" ==========
            System.out.println("========== POI 搜索 ==========");
            List<Poi> pois = client.searchPoi("肯德基", "上海");
            System.out.println("共返回 " + pois.size() + " 条（前 3 条）：");
            pois.stream().limit(3).forEach(poi ->
                    System.out.println("  - " + poi.getName()
                            + " @ " + poi.getAddress()
                            + "  [" + poi.getLocation() + "]")
            );

            // ========== 2. 天气查询：北京实时天气 ==========
            System.out.println("\n========== 天气查询（实时） ==========");
            WeatherLive live = client.queryWeatherLive("110000");
            if (live != null) {
                System.out.println("城市: " + live.getCity()
                        + "，天气: " + live.getWeather()
                        + "，温度: " + live.getTemperature() + "℃"
                        + "，风向: " + live.getWindDirection()
                        + "，风力: " + live.getWindPower() + "级"
                        + "，湿度: " + live.getHumidity() + "%"
                        + "，发布时间: " + live.getReportTime());
            }

            // ========== 3. 路径规划：从天安门到故宫的驾车路线 ==========
            System.out.println("\n========== 驾车路径规划 ==========");
            Route route = client.drivingRoute(
                    "116.397428,39.909230",   // 天安门
                    "116.403963,39.915119"    // 故宫
            );
            if (route != null && route.getPaths() != null && !route.getPaths().isEmpty()) {
                Path best = route.getPaths().get(0);
                System.out.println("总距离: " + best.getDistance() + " 米");
                System.out.println("预计耗时: " + best.getDuration() + " 秒");
                System.out.println("途经步骤数: "
                        + (best.getSteps() == null ? 0 : best.getSteps().size()));
            }

            // ========== 3.2 步行路径 ==========
            System.out.println("\n========== 步行路径规划 ==========");
            Route walk = client.walkingRoute("116.397428,39.909230", "116.403963,39.915119");
            if (walk != null && walk.getPaths() != null && !walk.getPaths().isEmpty()) {
                Path wp = walk.getPaths().get(0);
                System.out.println("总距离: " + wp.getDistance() + " 米，预计耗时: " + wp.getDuration() + " 秒");
            }

            // ========== 3.3 骑行路径（v4 接口） ==========
            System.out.println("\n========== 骑行路径规划 ==========");
            Route bike = client.bicyclingRoute("116.397428,39.909230", "116.434446,39.90816");
            if (bike != null && bike.getPaths() != null && !bike.getPaths().isEmpty()) {
                Path bp = bike.getPaths().get(0);
                System.out.println("总距离: " + bp.getDistance() + " 米，预计耗时: " + bp.getDuration() + " 秒");
            }

            // ========== 3.4 公交路径（北京地坛 → 天安门） ==========
            System.out.println("\n========== 公交路径规划 ==========");
            TransitRoute transit = client.transitRoute(
                    "116.418579,39.957714",   // 起点：地坛附近
                    "116.397428,39.909230",   // 终点：天安门
                    "北京", null);
            if (transit != null && transit.getTransits() != null && !transit.getTransits().isEmpty()) {
                System.out.println("共 " + transit.getTransits().size() + " 个换乘方案，前 2 个：");
                transit.getTransits().stream().limit(2).forEach(t ->
                        System.out.println("  - 耗时 " + t.getDuration() + "秒"
                                + "，费用 " + t.getCost() + "元"
                                + "，步行 " + t.getWalkingDistance() + "米"
                                + "，换乘段数 " + (t.getSegments() == null ? 0 : t.getSegments().size())));
            }

            // ========== 4. 地理编码：地址 → 经纬度 ==========
            System.out.println("\n========== 地理编码（地址 → 坐标） ==========");
            List<Geocode> geocodes = client.geocode("上海市徐汇区肇嘉浜路1065号", null);
            if (!geocodes.isEmpty()) {
                Geocode g = geocodes.get(0);
                System.out.println("规整地址: " + g.getFormattedAddress());
                System.out.println("经纬度: " + g.getLocation());
                System.out.println("adcode: " + g.getAdcode() + "（" + g.getLevel() + "）");
            }

            // ========== 5. 逆地理编码：经纬度 → 地址 ==========
            System.out.println("\n========== 逆地理编码（坐标 → 地址） ==========");
            ReGeocode regeo = client.regeocode("116.481028,39.989643");
            if (regeo != null) {
                System.out.println("规整地址: " + regeo.getFormattedAddress());
                System.out.println("adcode: " + regeo.getAdcode());
                System.out.println("地址细分: " + regeo.getAddressComponent());
            }

            // ========== 6. 组合用法：地址 → adcode → 天气 ==========
            System.out.println("\n========== 组合用法：按地址查天气 ==========");
            String address = "广州市天河区";
            List<Geocode> list = client.geocode(address, null);
            if (!list.isEmpty()) {
                String adcode = list.get(0).getAdcode();
                System.out.println("[" + address + "] 对应 adcode = " + adcode);
                WeatherLive w = client.queryWeatherLive(adcode);
                if (w != null) {
                    System.out.println("当前天气: " + w.getWeather()
                            + "，温度: " + w.getTemperature() + "℃"
                            + "，风力: " + w.getWindPower() + "级");
                }
            }

        } catch (Exception e) {
            System.err.println("调用高德 API 出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}