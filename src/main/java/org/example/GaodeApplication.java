package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot 启动类。
 * <p>
 * 启动后默认监听 8080 端口，访问示例：
 * <pre>
 *   http://localhost:8080/api/poi?keywords=肯德基&amp;city=上海
 *   http://localhost:8080/api/weather/live?adcode=110000
 *   http://localhost:8080/api/route/driving?origin=116.397,39.909&amp;destination=116.403,39.915
 *   http://localhost:8080/api/geocode?address=上海市徐汇区肇嘉浜路1065号
 *   http://localhost:8080/api/regeocode?location=116.481028,39.989643
 * </pre>
 * <p>
 * 原 {@link Main} 类保留为 <b>纯 Java 命令行示例</b>，不依赖 Spring 容器，
 * 证明 {@link AmapClient} 的核心逻辑与框架解耦。
 */
@SpringBootApplication
@ConfigurationPropertiesScan   // 扫描带 @ConfigurationProperties 的类，省去手动 @EnableConfigurationProperties
public class GaodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GaodeApplication.class, args);
    }
}
