FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine AS builder
WORKDIR /app
# 必要的套件
RUN apk add --no-cache tzdata
# 複製 JAR 檔案
COPY target/*.jar app.jar
# 設定時區
ENV TZ=Asia/Taipei
# 用於上傳的目錄
RUN mkdir -p /app/uploads /app/logs && chmod 777 /app/uploads /app/logs
# 非 root 使用者
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
# 運行應用程式
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
