# ---------- Build stage ----------
FROM gradle:8.10.2-jdk17-alpine AS build
WORKDIR /workspace
# 전체 소스 복사 (gradle wrapper 없어도 gradle 명령으로 동작)
COPY . .
# 테스트 스킵하고 부트 JAR 빌드
RUN gradle -g /home/gradle/.gradle bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jdk-alpine
# 생성된 JAR 복사 (SNAPSHOT 버전 그대로 사용 중이므로 매치됨)
COPY --from=build /workspace/build/libs/*SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
