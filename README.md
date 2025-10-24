# Minimal Spring Boot Monorepo

This repository contains a four-module Spring Boot skeleton (client, booking, loyalty, gateway) plus an Angular SPA (`comb-frontend`) that consumes the gateway APIs. Each module only exposes an `Application` entry point plus a smoke test that ensures the Spring application context loads.  
- The `client-service` includes an event-sourced CSV ingestion endpoint and publishes client lifecycle events to Kafka so other services stay in sync without direct HTTP calls.
- The `gateway-service` exposes friendly paths (`/clients/**`, `/appointments/**`, `/services/**`, `/purchases/**`, `/loyalty/**`, `/import/**`) and forwards requests to the appropriate downstream microservice.
- The Angular frontend offers a secure dashboard for CSV imports, loyalty leaderboards, and client metrics. See `comb-frontend/README.md` for setup instructions.

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

### Client Service REST API

The client-service now exposes REST endpoints backed by the event store:

- `GET /api/v1/clients` – paginated list of non-banned clients (`page`, `size` query params).
- `GET /api/v1/clients/{id}` – fetch a single non-banned client by id.
- `PUT /api/v1/clients/{id}` – update client details (emits a `CLIENT_UPDATED` event when data changes).
- `DELETE /api/v1/clients/{id}` – bans the client by appending a `CLIENT_BANNED` event.

## Docker Compose Stack

Each microservice can be containerised alongside its own MongoDB instance and a shared Kafka broker. To build the jars and boot the full stack:

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

### Kafka Event Streaming

- The `client-service` writes every `CLIENT_REGISTERED`, `CLIENT_UPDATED`, or `CLIENT_BANNED` event to the `client-events` Kafka topic after persisting it in MongoDB.
- `booking-service` and `loyalty-service` consume that topic (each with its own consumer group) and persist the events to their local `client_events` collections. This gives every service its own event store and keeps them decoupled from synchronous calls.
- To disable Kafka during tests, the profile-specific configuration sets `client.kafka.enabled=false`, which activates a no-op publisher/consumer implementation.
### Client CSV Import

The client-service exposes `POST /api/v1/import/clients` which accepts a `multipart/form-data` upload containing a `clients.csv` file.  
For every row the service appends a new event to the `client_events` MongoDB collection, emitting either `CLIENT_REGISTERED` or `CLIENT_UPDATED` depending on the historical state of the client.
