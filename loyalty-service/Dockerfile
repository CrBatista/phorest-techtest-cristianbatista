FROM openjdk:21-jdk-slim

WORKDIR /app

COPY loyalty-service/target/loyalty-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
