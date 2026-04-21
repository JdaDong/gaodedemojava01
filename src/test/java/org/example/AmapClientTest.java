package org.example;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.example.model.Geocode;
import org.example.model.Path;
import org.example.model.Poi;
import org.example.model.ReGeocode;
import org.example.model.Route;
import org.example.model.WeatherLive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AmapClient} 单元测试
 * <p>
 * 通过 OkHttp {@link MockWebServer} 模拟高德接口，<b>不消耗任何 API 配额</b>。
 */
class AmapClientTest {

    private MockWebServer server;
    private AmapClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        // MockWebServer 的 URL 形如 http://localhost:xxxxx/ ，去掉末尾斜杠
        String baseUrl = server.url("/v3").toString();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        client = new AmapClient("TEST_KEY", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    // ====================== 1. POI 搜索 ======================

    @Test
    @DisplayName("POI 搜索：正常返回 → 解析为 List<Poi>")
    void searchPoi_success() throws Exception {
        server.enqueue(new MockResponse().setBody("""
                {
                  "status": "1",
                  "info": "OK",
                  "infocode": "10000",
                  "count": "2",
                  "pois": [
                    {"id":"B1","name":"肯德基(和平店)","type":"餐饮","address":"西藏中路268号","location":"121.48,31.23","pname":"上海市","cityname":"上海市","adname":"黄浦区"},
                    {"id":"B2","name":"肯德基(汇川店)","type":"餐饮","address":"长宁路1018号","location":"121.41,31.22","pname":"上海市","cityname":"上海市","adname":"长宁区"}
                  ]
                }
                """));

        List<Poi> pois = client.searchPoi("肯德基", "上海");

        assertEquals(2, pois.size());
        assertEquals("肯德基(和平店)", pois.get(0).getName());
        assertEquals("121.48,31.23", pois.get(0).getLocation());
        assertEquals("黄浦区", pois.get(0).getDistrictName());

        // 验证请求参数正确组装
        RecordedRequest req = server.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(req);
        assertTrue(req.getPath().startsWith("/v3/place/text"));
        assertTrue(req.getPath().contains("key=TEST_KEY"));
        assertTrue(req.getPath().contains("keywords=%E8%82%AF%E5%BE%B7%E5%9F%BA"));
        assertTrue(req.getPath().contains("city=%E4%B8%8A%E6%B5%B7"));
    }

    @Test
    @DisplayName("POI 搜索：pois 为空数组 → 返回空列表，不 NPE")
    void searchPoi_emptyList() throws Exception {
        server.enqueue(new MockResponse().setBody("""
                {"status":"1","info":"OK","infocode":"10000","count":"0","pois":[]}
                """));

        List<Poi> pois = client.searchPoi("不存在的地点", null);
        assertTrue(pois.isEmpty());
    }

    // ====================== 2. 天气查询 ======================

    @Test
    @DisplayName("实时天气：正常返回 → 解析 WeatherLive")
    void queryWeatherLive_success() throws Exception {
        server.enqueue(new MockResponse().setBody("""
                {
                  "status": "1",
                  "info": "OK",
                  "infocode": "10000",
                  "lives": [{
                    "province":"北京","city":"北京市","adcode":"110000",
                    "weather":"多云","temperature":"25",
                    "winddirection":"南","windpower":"≤3","humidity":"45",
                    "reporttime":"2026-04-21 15:00:00"
                  }]
                }
                """));

        WeatherLive live = client.queryWeatherLive("110000");

        assertNotNull(live);
        assertEquals("北京市", live.getCity());
        assertEquals("多云", live.getWeather());
        assertEquals("25", live.getTemperature());
        assertEquals("南", live.getWindDirection());
    }

    @Test
    @DisplayName("实时天气：lives 空数组 → 返回 null")
    void queryWeatherLive_emptyLives_returnsNull() throws Exception {
        server.enqueue(new MockResponse().setBody("""
                {"status":"1","info":"OK","infocode":"10000","lives":[]}
                """));
        assertEquals(null, client.queryWeatherLive("999999"));
    }

    // ====================== 3. 驾车路径规划 ======================

    @Test
    @DisplayName("驾车路径规划：正常返回 → 解析 Route/Path/Step")
    void drivingRoute_success() throws Exception {
        server.enqueue(new MockResponse().setBody("""
                {
                  "status":"1","info":"OK","infocode":"10000",
                  "route": {
                    "origin":"116.397,39.909","destination":"116.403,39.915","taxi_cost":"13",
                    "paths":[{
                      "distance":"1199","duration":"581","strategy":"速度优先",
                      "tolls":"0","toll_distance":"0",
                      "steps":[
                        {"instruction":"沿长安街向东","road":"长安街","distance":"500","duration":"120","polyline":"xxx","action":"直行"},
                        {"instruction":"到达目的地","road":"","distance":"699","duration":"461","polyline":"yyy","action":""}
                      ]
                    }]
                  }
                }
                """));

        Route route = client.drivingRoute("116.397,39.909", "116.403,39.915");

        assertNotNull(route);
        assertEquals(1, route.getPaths().size());
        Path p = route.getPaths().get(0);
        assertEquals("1199", p.getDistance());
        assertEquals("581", p.getDuration());
        assertEquals(2, p.getSteps().size());
        assertEquals("长安街", p.getSteps().get(0).getRoad());
    }

    // ====================== 4. 地理编码 ======================

    @Test
    @DisplayName("地理编码：地址 → 坐标，含 adcode")
    void geocode_success() throws Exception {
        server.enqueue(new MockResponse().setBody("""
                {
                  "status":"1","info":"OK","infocode":"10000","count":"1",
                  "geocodes":[{
                    "formatted_address":"上海市徐汇区肇嘉浜路1065号",
                    "province":"上海市","city":"上海市","district":"徐汇区",
                    "adcode":"310104","citycode":"021",
                    "location":"121.4505,31.1957","level":"门牌号"
                  }]
                }
                """));

        List<Geocode> list = client.geocode("上海市徐汇区肇嘉浜路1065号", null);

        assertEquals(1, list.size());
        Geocode g = list.get(0);
        assertEquals("310104", g.getAdcode());
        assertEquals("门牌号", g.getLevel());
        assertEquals("121.4505,31.1957", g.getLocation());
    }

    // ====================== 5. 逆地理编码（含直辖市 city=[] 坑） ======================

    @Test
    @DisplayName("逆地理编码：直辖市 city 返回空数组时不应反序列化失败")
    void regeocode_directMunicipality_cityIsEmptyArray() throws Exception {
        server.enqueue(new MockResponse().setBody("""
                {
                  "status":"1","info":"OK","infocode":"10000",
                  "regeocode": {
                    "formatted_address":"北京市朝阳区望京街道",
                    "addressComponent": {
                      "province":"北京市",
                      "city":[],
                      "citycode":"010",
                      "district":"朝阳区",
                      "adcode":"110105",
                      "township":"望京街道",
                      "streetNumber":{}
                    }
                  }
                }
                """));

        ReGeocode regeo = client.regeocode("116.481028,39.989643");

        assertNotNull(regeo);
        assertEquals("110105", regeo.getAdcode());
        assertEquals("朝阳区", regeo.getAddressComponent().getDistrictStr());
        // ⚠️ 关键：空数组应该安全转为 ""
        assertEquals("", regeo.getAddressComponent().getCityStr());
    }

    // ====================== 6. 异常路径 ======================

    @Test
    @DisplayName("API 失败（status=0）→ 抛 AmapException 并携带 infocode/info")
    void apiFailure_throwsAmapException() {
        server.enqueue(new MockResponse().setBody("""
                {"status":"0","info":"INVALID USER KEY","infocode":"10001"}
                """));

        AmapException ex = assertThrows(AmapException.class,
                () -> client.searchPoi("测试", null));

        assertEquals("10001", ex.getInfoCode());
        assertEquals("INVALID USER KEY", ex.getInfo());
        assertEquals("POI 搜索", ex.getApiName());
    }

    @Test
    @DisplayName("HTTP 500 → 抛 IOException")
    void httpError_throwsIOException() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("Server Error"));

        assertThrows(IOException.class,
                () -> client.searchPoi("测试", null));
    }

    @Test
    @DisplayName("配额超限（infocode=10003）→ AmapException，方便上层做降级")
    void quotaExceeded_throwsAmapException() {
        server.enqueue(new MockResponse().setBody("""
                {"status":"0","info":"DAILY_QUERY_OVER_LIMIT","infocode":"10003"}
                """));

        AmapException ex = assertThrows(AmapException.class,
                () -> client.queryWeatherLive("110000"));
        assertEquals("10003", ex.getInfoCode());
    }
}
