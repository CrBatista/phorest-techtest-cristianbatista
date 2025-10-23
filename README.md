# Minimal Spring Boot Monorepo

This repository contains a four-module Spring Boot skeleton (client, booking, loyalty, gateway). Each module only exposes an `Application` entry point plus a smoke test that ensures the Spring application context loads.  
The `client-service` now includes an event-sourced CSV ingestion endpoint for client data.

## Building and Testing

```bash
mvn clean verify
```

## Running a Service

After building, launch any service with:

```bash
mvn -pl client-service spring-boot:run
```

Replace `client-service` with the desired module (`booking-service`, `loyalty-service`, or `gateway-service`).

### Client CSV Import

The client-service exposes `POST /api/v1/import/clients` which accepts a `multipart/form-data` upload containing a `clients.csv` file.  
For every row the service appends a new event to the `client_events` MongoDB collection, emitting either `CLIENT_REGISTERED` or `CLIENT_UPDATED` depending on the historical state of the client.
