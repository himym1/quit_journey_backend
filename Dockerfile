# 多阶段构建 Dockerfile
# 阶段1: 构建阶段
FROM amazoncorretto:17-alpine AS builder

WORKDIR /app

# 复制 Gradle 构建文件
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/
COPY gradlew ./

# 赋予执行权限
RUN chmod +x gradlew

# 下载依赖（利用Docker缓存）
RUN ./gradlew dependencies --no-daemon

# 复制源代码
COPY src/ src/

# 构建应用
RUN ./gradlew build -x test --no-daemon

# 阶段2: 运行阶段
FROM amazoncorretto:17-alpine

WORKDIR /app

# 创建非root用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 安装curl用于健康检查
RUN apk add --no-cache curl

# 从构建阶段复制JAR文件
COPY --from=builder /app/build/libs/*.jar app.jar

# 更改文件所有者
RUN chown appuser:appgroup app.jar

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
