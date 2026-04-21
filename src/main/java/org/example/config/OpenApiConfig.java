package org.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI（Swagger UI）配置。
 *
 * <p>启动后访问：</p>
 * <ul>
 *   <li><a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a> — 可视化 UI</li>
 *   <li><a href="http://localhost:8080/v3/api-docs">http://localhost:8080/v3/api-docs</a> — OpenAPI JSON 元数据</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gaodeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("高德地图 Demo API")
                        .description("基于 Spring Boot 3.3.4 + OkHttp + Fastjson2 封装的高德开放平台 REST 接口，"
                                + "包含 POI 搜索、天气查询、驾车路径规划、地理编码 / 逆地理编码。")
                        .version("1.0-SNAPSHOT")
                        .contact(new Contact()
                                .name("gaodedemojava01")
                                .url("https://lbs.amap.com/api/webservice/summary"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
