# workOut — 多阶段构建：前端静态资源打进 Spring Boot jar，单容器对外 HTTP :8080
# 用法：
#   docker build -t workout:local .
#   docker run --rm -p 8080:8080 workout:local
# 默认连 application.yml / docker profile 同一套 MySQL（SQLPub）；可用 WORKOUT_DB_* 覆盖。

# —— Stage 1: 构建 React SPA ——
FROM node:20-alpine AS frontend
WORKDIR /build/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# —— Stage 2: Maven 打包（含静态资源）——
FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /build/backend
# 阿里云 Maven 镜像，加速 Docker 构建依赖下载
COPY backend/docker/maven-settings.xml /root/.m2/settings.xml
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /build/frontend/dist ./src/main/resources/static
RUN mvn -q -B -DskipTests package \
    && mv target/workout-*.jar /build/app.jar

# —— Stage 3: 运行时 ——
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd -r workout \
    && useradd -r -g workout workout \
    && chown -R workout:workout /app
COPY --from=backend /build/app.jar /app/app.jar
USER workout
EXPOSE 8080
# docker profile 与本地开发共用同一套 MySQL datasource（非内嵌 H2）
ENV SPRING_PROFILES_ACTIVE=docker
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080 && printf "GET /api/v1/health HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n" >&3 && cat <&3 | grep -q UP'
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
