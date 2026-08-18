# workOut — 多阶段构建：前端静态资源打进 Spring Boot jar，单容器对外 HTTP :8080
# 用法：
#   docker build -t workout:local .
#   docker run --rm -p 8080:8080 workout:local

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
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /build/frontend/dist ./src/main/resources/static
RUN mvn -q -B -DskipTests package \
    && mv target/workout-*.jar /build/app.jar

# —— Stage 3: 运行时 ——
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN mkdir -p /data \
    && groupadd -r workout \
    && useradd -r -g workout workout \
    && chown -R workout:workout /data /app
COPY --from=backend /build/app.jar /app/app.jar
USER workout
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker \
    WORKOUT_H2_PATH=/data/workout
VOLUME ["/data"]
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080 && printf "GET /api/v1/health HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n" >&3 && cat <&3 | grep -q UP'
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
