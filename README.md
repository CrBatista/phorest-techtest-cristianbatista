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
