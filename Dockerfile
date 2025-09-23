# ---------- Build stage ----------
FROM gradle:8.10.2-jdk17-alpine AS build
WORKDIR /workspace
COPY . .
RUN gradle -g /home/gradle/.gradle bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /workspace/build/libs/*SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
