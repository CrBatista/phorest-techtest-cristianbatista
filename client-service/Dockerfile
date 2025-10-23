# Build Stage: rely on pre-built jar copied from local build
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY client-service/target/client-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
