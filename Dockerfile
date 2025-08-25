# Dockerfile
FROM eclipse-temurin:17-jdk-alpine

# 빌드된 JAR 파일 경로를 지정
ARG JAR_FILE=build/libs/capstone_shop-0.0.1-SNAPSHOT.jar

# JAR 파일을 컨테이너에 복사
COPY ${JAR_FILE} app.jar

# 실행
ENTRYPOINT ["java","-jar","/app.jar"]
