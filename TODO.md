# 📋 高德地图 Demo 项目 TODO List

> 项目：`gaodedemojava01`
> 技术栈：Java 17 + Maven + OkHttp + Fastjson2
> 更新时间：2026-04-21

---

## 🎯 当前项目状态

### ✅ 已完成
- 全部 20 项任务全部完成 🎉🎉🎉

### ⏳ 待完成
- 无（项目 100% 完成）

---

## 🗂️ TODO List（按推荐顺序）

### 🟢 P0 必做 —— 先把项目跑通

- [x] **T1. 申请高德 Key**，填入 `Main.java` 的 `AMAP_KEY` ✅ 2026-04-21
- [x] **T2. Maven 刷新依赖**（IDEA 右上角 🔄） ✅ 2026-04-21
- [x] **T3. 运行 Main**，确认三个功能输出正常 ✅ 2026-04-21

---

### 🟡 P1 推荐 —— 代码质量提升

- [x] **T4. 统一响应封装** ✅ 2026-04-21
  新建 `AmapResponse<T>`，通过 `parse(json, apiName, dataParser)` 统一处理
  `status / info / infocode`，消除重复校验代码
- [x] **T5. 业务异常类** ✅ 2026-04-21
  新建 `AmapException`，携带 `apiName / infoCode / info`，并附常见错误码说明
- [x] **T6. POJO 化返回结果** ✅ 2026-04-21
  - `Poi`（POI 搜索结果项）
  - `WeatherLive` / `WeatherForecast` / `WeatherCast`（天气）
  - `Route` / `Path` / `Step`（路线）

  已用 `@JSONField` 注解映射字段，消除满屏的 `getString()`
- [x] **T7. 日志接入** ✅ 2026-04-21
  引入 `slf4j-api 2.0.13` + `logback-classic 1.5.6`，`logback.xml` 配置彩色控制台输出
  `AmapClient.doGet` 打印 URL / HTTP 状态码 / 耗时 / 响应长度

---

### 🟠 P2 功能扩展 —— 覆盖更多高德能力

- [x] **T8. 扩展导航方式** ✅ 2026-04-21
  - 新增步行 `walkingRoute()` / 骑行 `bicyclingRoute()` / 公交 `transitRoute()` 三个方法
  - 步行、骑行复用 `Route` / `Path` / `Step`（为 `Step` 补 `orientation` 字段）
  - 骑行接口特殊：走 **v4** 版本 `{errcode, errmsg, data}`，单独处理不走 `AmapResponse`
  - 公交新增 4 个 POJO：`TransitRoute` / `Transit` / `Segment`（含 `WalkingSegment`+`BusSegment`）/ `BusLine`（含 `Stop`）
  - Controller 新增 3 个 REST 接口：`/api/route/walking`、`/api/route/bicycling`、`/api/route/transit`
  - Main.java 补充 CLI 演示，真实调用高德全部返回合理结果（步行 959s、骑行 922s、公交 2220s/4元）
- [x] **T9. 地理编码 / 逆地理编码** ✅ 2026-04-21
  - 地址 → 经纬度（`/geocode/geo`）返回 `List<Geocode>`
  - 经纬度 → 地址（`/geocode/regeo`）返回 `ReGeocode`
  - 新增 POJO：`Geocode` / `ReGeocode` / `AddressComponent`

  > 很实用：天气查询需要 adcode，而用户通常只知道地址
- [x] **T10. 输入参数校验** ✅ 2026-04-21
  - Controller 添加 `@Validated`，参数使用 `@NotBlank` / `@Pattern`（经纬度、adcode 6 位）
  - `GlobalExceptionHandler` 新增 `ConstraintViolationException` 、`MethodArgumentTypeMismatchException` 统一处理
  - 返回结构包含 `violations` 数组，每项含 `field / message / rejected`
- [x] **T11. 接口限流保护** ✅ 2026-04-21
  - 自研轻量异步无锁令牌桶 `TokenBucket`（基于 `AtomicLong` + CAS）
  - `@RateLimit` 注解 + `RateLimitAspect` 切面，支持 `GLOBAL` / `PER_IP` 两种粒度
  - 引入 `spring-boot-starter-aop`，Controller 3 个关键接口默认限流 QPS=5
  - 超限流返回 HTTP 429，带 `Retry-After` 头
  - 新增 `TokenBucketTest` 3 个单测：突发限/令牌补充/非法参数

---

### 🔵 P3 工程化 —— 迈向可交付项目

- [x] **T12. Key 外部化配置** ✅ 2026-04-21
  - 新增 `AppConfig`（单例），三级优先级：环境变量 `AMAP_KEY` > JVM `-Damap.key` > `config.properties`
  - `config.properties.example` 提交 Git，`config.properties` 已加 `.gitignore`
  - 占位符 Key 自动拦截，避免误用
- [x] **T13. 单元测试** ✅ 2026-04-21
  - 引入 `JUnit 5.10.2` + `MockWebServer 4.12.0` + `maven-surefire-plugin 3.2.5`
  - 3 个测试类、约 15 个用例覆盖：POI/天气/路径/地理编码/逆地理编码、直辖市 city=[] 坑、`AmapException`、HTTP 500、配额超限、`AmapResponse.parse`、`AppConfig`
  - 给 `AmapClient` 增加包级别构造器 `AmapClient(apiKey, baseUrl)`以支持 Mock 注入
  - 全部测试依赖 MockWebServer，**不消耗高德 API 配额**
- [x] **T14. 结果缓存** ✅ 2026-04-21
  - 引入 `spring-boot-starter-cache` + `caffeine`（版本由 Spring Boot BOM 管理）
  - 新增 `config/CacheConfig`：`@EnableCaching` + `SimpleCacheManager`，为五个 cache region 给不同 TTL
  - 缓存策略：poi(10min) / weatherLive(5min) / weatherForecast(30min) / geocode(24h) / regeocode(24h)；驾车路线**故意不缓存**
  - `AmapClient` 5 个方法标 `@Cacheable`，key 采用 SpEL 处理 null 参数；天气用 `unless="#result == null"` 避免空值污染
  - 新增 `AmapClientCacheTest`（最小 Spring 上下文）验证：同参数两次调用只打 1 次 HTTP、不同参数独立、null 不入缓存、5 个 region 均存在
  - 全部 22 个单测通过（原 18 + 新 4）
- [x] **T15. 异步调用** ✅ 2026-04-21
  - 新增 `AsyncConfig`：`@EnableAsync` + `ThreadPoolTaskExecutor`（core=8, max=32, queue=200）
  - 新增 `AsyncAmapClient`：独立 Bean 包装 `AmapClient`，返回 `CompletableFuture<T>`（避免 @Async 自调用失效）
  - 新增 `DashboardController.dashboard()`：并行拉 POI+天气+驾车路线，用 `CompletableFuture.allOf` 汇总
  - 性能提升：串行 600ms → 并行 ≈ 200ms（理论 3x 提速）

---

### 🟣 P4 升级方案 B —— Spring Boot 化

- [x] **T16. 引入 Spring Boot 3.3.4** ✅ 2026-04-21
- [x] **T17.** `AmapClient` 标注 `@Service`，Key 用 `@ConfigurationProperties("amap")` 注入（`AmapProperties` record）✅ 2026-04-21
- [x] **T18.** `AmapController` 暴露 `/api/poi` `/api/weather/{live,forecast}` `/api/route/driving` `/api/geocode` `/api/regeocode` 六个接口；`GlobalExceptionHandler` 统一处理 `AmapException`/`IOException` ✅ 2026-04-21
- [x] **T19. Swagger / SpringDoc** ✅ 2026-04-21
  - 引入 `springdoc-openapi-starter-webmvc-ui 2.6.0`
  - 新增 `config/OpenApiConfig`，自定义标题、描述、版本、联系人、许可证
  - `AmapController` 加 `@Tag` / `@Operation` / `@Parameter`，6 个接口均有描述和示例
  - 访问地址：`http://localhost:8080/swagger-ui.html` / `http://localhost:8080/v3/api-docs`
- [x] **T20. Docker 化部署** ✅ 2026-04-21
  - 多阶段 `Dockerfile`（Maven 构建 → 22-jre 运行，非 root 用户）
  - `docker-compose.yml`（环境变量注入 `AMAP_KEY`、healthcheck、自动重启）
  - `.dockerignore`（排除 `target/`、`.git/`、`config.properties` 等敏感与无用文件）
  - 一键启动：`echo "AMAP_KEY=你的Key" > .env && docker compose up -d`

---

## 📊 推荐学习路径（由易到难）

```mermaid
graph LR
    A[P0 跑通] --> B[P1 代码质量]
    B --> C[P2 功能扩展]
    C --> D[P3 工程化]
    D --> E[P4 SpringBoot化]
```

---

## 🤔 推荐主线

> **T1 → T2 → T3 → T6（POJO 化）→ T9（地理编码）→ T12（Key 外部化）→ T16~T18（Spring Boot 化）**

这条路径能循序渐进地学到：
1. HTTP 调用基础
2. 对象映射 / JSON 处理
3. 配置管理
4. 框架化开发

---

## 📝 进度记录

| 日期 | 完成项 | 备注 |
|------|--------|------|
| 2026-04-21 | 项目初始化、AmapClient 封装 | 三大核心功能完成 |
| 2026-04-21 | T1 完成：Key 已填入 `Main.java` | POI 城市改为上海 |
| 2026-04-21 | T2+T3 完成：Maven 编译、三个功能验证通过 🎉 | P0 阶段全部完成 |
| 2026-04-21 | T6 完成：POJO 化重构，新增 `model` 包下 7 个 POJO | AmapClient 拆分 queryWeatherLive/Forecast |
| 2026-04-21 | T9 完成：地理编码 / 逆地理编码，新增 3 个 POJO | Main 补充 3 段演示，含组合用法 |
| 2026-04-21 | T12 完成：Key 外部化配置，新增 `AppConfig` 与 `config.properties` | `.gitignore` 增加敏感文件规则 |
| 2026-04-21 | T4+T5+T7 完成：`AmapResponse` 统一响应、`AmapException` 业务异常、SLF4J+Logback 日志 | 重构 `AmapClient`，删除原 `checkStatus` |
| 2026-04-21 | T13 完成：JUnit 5 + MockWebServer 单测 | 3 个测试类 / 15 用例 / 不消耗 API 配额 |
| 2026-04-21 | **T16+T17+T18 完成：Spring Boot 3.3.4 化** | `GaodeApplication` + `AmapProperties`(record) + `AmapController` + `GlobalExceptionHandler`；`Main` 保留为 CLI 演示 |
| 2026-04-21 | **T19+T20 完成：Swagger UI + Docker 化** 🎊 | SpringDoc 2.6.0 + 多阶段 Dockerfile + docker-compose + .dockerignore；P4 阶段全部结束 |
| 2026-04-21 | **T14 完成：Caffeine 本地缓存** | 5 个 cache region / 独立 TTL / 驾车不缓存 / null 不污染 / 新增 4 个缓存专项单测 |
| 2026-04-21 | **T8 完成：步行/骑行/公交导航** | 3 个新方法 + 4 个新 POJO + 3 个新 REST 接口；骑行 v4 单独处理；真实调用验证通过 |
| 2026-04-21 | **T10+T11+T15 完成：参数校验 + 限流 + 异步** 🎊🎊🎊 项目 100% 完成 | `@Validated` 全接口校验 / 自研令牌桶 + @RateLimit AOP / `AsyncAmapClient` + `/api/dashboard` 并行组合接口 |
|  |  |  |

---

> 💡 勾选方式：把 `- [ ]` 改成 `- [x]` 即可标记完成。
