#Stage 1: builder
FROM gradle:8.8.0-jdk21-jammy AS builder
WORKDIR /app
#Cache
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

RUN ./gradlew dependencies --no-daemon

COPY . .

# Lệnh 'bootJar' hiệu quả hơn 'build' vì nó chỉ tập trung vào việc tạo file jar có thể chạy được, -x bỏ qua unit test
RUN ./gradlew bootJar --no-daemon -x test

#Stage 2: Final
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ARG MODULE_PATH
COPY --from=builder /app/${MODULE_PATH}/build/libs/*.jar application.jar
EXPOSE 8080

# Lệnh để khởi động ứng dụng khi container chạy
ENTRYPOINT ["java", "-jar", "/app/application.jar"]