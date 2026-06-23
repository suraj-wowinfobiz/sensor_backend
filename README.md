# backend-sensor

This folder contains a single Spring Boot monolith assembled from the existing backend services in this repository.

What it does:

- boots one application process for `authentication`, `organization`, `device-managment`, `ingestion`, `processing`, `analytics`, `configuration`, and `threshold-alert`
- keeps the original package structure inside `backend-sensor/src/main/java`
- replaces service discovery, gateway routing, and per-service bootstrap classes with one launcher and one shared runtime config

Important consolidation choices:

- `Eureka-Server` and `api-gateway-iot` are not part of the monolith runtime
- the duplicate device-management `IngestionController` is excluded so the main ingestion API can own `/api/v1/ingestion/**`
- service-local Kafka config classes are excluded and replaced with one shared Kafka config
- authentication security is replaced with a servlet security chain that mirrors the old gateway access rules

Run it with:

```bash
cd backend-sensor
./mvnw spring-boot:run
```

If you prefer the system Maven:

```bash
cd backend-sensor
mvn spring-boot:run
```
# sensor_backend
