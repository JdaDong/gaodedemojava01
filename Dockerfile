# syntax=docker/dockerfile:1.6
# ==========================================================================
# 高德地图 Demo · 多阶段 Dockerfile
# 阶段 1: 使用 Maven + JDK 22 构建 fat-jar
# 阶段 2: 使用精简 JRE 运行，镜像更小 (~200MB vs. 600MB+)
# 构建:  docker build -t gaodedemo:latest .
# 运行:  docker run --rm -p 8080:8080 -e AMAP_KEY=你的Key gaodedemo:latest
# ==========================================================================

# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-22 AS builder

WORKDIR /build

# 先拷贝 pom.xml 单独下载依赖，利用 Docker 层缓存：源码变动时不必重下依赖
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests dependency:go-offline

# 再拷贝源码并打包（跳过测试以加快构建；如需运行测试可去掉 -DskipTests）
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:22-jre

# 元信息（可选，方便 docker inspect 查看）
LABEL org.opencontainers.image.title="gaode-demo-java"
LABEL org.opencontainers.image.description="高德地图 Demo (Spring Boot 3.3.4 + OkHttp + Fastjson2)"
LABEL org.opencontainers.image.source="https://github.com/your-org/gaodedemojava01"

# 非 root 用户运行，提升安全性
RUN groupadd --system app && useradd --system --gid app --home-dir /app app
WORKDIR /app

# 仅拷贝最终 fat-jar；通配符匹配 1.0-SNAPSHOT 等版本号变化
COPY --from=builder /build/target/gaodedemojava01-*.jar /app/app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080

# JVM 参数：容器感知、UTF-8、时区、合适的内存策略
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Dfile.encoding=UTF-8 \
               -Duser.timezone=Asia/Shanghai"

# 用 sh -c 以便 $JAVA_OPTS 能被 shell 展开
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
