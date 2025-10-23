# Minimal Spring Boot Monorepo

This repository contains a four-module Spring Boot skeleton (client, booking, loyalty, gateway). Each module only exposes an `Application` entry point plus a smoke test that ensures the Spring application context loads.

## Building and Testing

```bash
export PATH=/Users/ivangonzalez/Phorest/MVN/mvn/bin:$PATH  # if Maven is not already on PATH
mvn clean verify
```

## Running a Service

After building, launch any service with:

```bash
mvn -pl client-service spring-boot:run
```

Replace `client-service` with the desired module (`booking-service`, `loyalty-service`, or `gateway-service`).

## Docker Compose Stack

Each microservice can be containerised alongside its own MongoDB instance. To build the jars and boot the full stack:

```bash
mvn clean package -DskipTests
docker compose -f ops/docker-compose.yml build
docker compose -f ops/docker-compose.yml up -d
```

To stop and remove everything (including volumes):

```bash
docker compose -f ops/docker-compose.yml down --volumes --remove-orphans
```

You can also launch individual services with their dedicated files, for example:

```bash
docker compose -f ops/docker-compose.client-service.yml up -d
```
