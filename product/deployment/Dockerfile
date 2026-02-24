# ---------- Stage 1: Build ----------
FROM --platform=linux/arm64 gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY libs ./libs
COPY . .

RUN gradle clean build -x test

# ---------- Stage 2: Runtime ----------
FROM --platform=linux/arm64 eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=builder /app/applications/app-service/build/libs/*.jar app.jar
COPY --from=builder /app/libs /app/libs

EXPOSE 8080

ENTRYPOINT ["java", "-Dloader.path=/app/lib", "-jar", "app.jar"]
