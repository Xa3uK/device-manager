# Device Manager

A REST API for managing devices built with Spring Boot 4 and PostgreSQL.

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.5** (Spring MVC, Spring Data JPA)
- **PostgreSQL** — persistent storage
- **Flyway** — database migrations
- **Lombok** — boilerplate reduction
- **springdoc-openapi** — API documentation
- **Spring Boot Actuator** — health and monitoring endpoints
- **Testcontainers** — integration testing against real PostgreSQL

## Running with Docker

**Requires:** Docker Desktop

```bash
docker compose up --build
```

This starts two containers:
- `device-manager-db` — PostgreSQL 17 on port `5432`
- `device-manager-app` — Spring Boot app on port `8080`

The app waits for the database to be healthy before starting. Flyway applies all migrations automatically on startup, including seed data with 150 sample devices.

To stop and remove containers:

```bash
docker compose down
```

To also remove the persisted database volume:

```bash
docker compose down -v
```

## Running Locally

**Requires:** Java 21 and PostgreSQL 17

Start a PostgreSQL instance and create the database:

```sql
CREATE DATABASE device_manager;
```

Run the application:

```bash
./gradlew bootRun
```

The app connects to `jdbc:postgresql://localhost:5432/device_manager` with credentials `postgres/postgres` by default. Override via environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/device_manager \
SPRING_DATASOURCE_USERNAME=your_user \
SPRING_DATASOURCE_PASSWORD=your_password \
./gradlew bootRun
```

## API Documentation

Swagger UI is available at: http://localhost:8080/swagger-ui.html

OpenAPI JSON spec: http://localhost:8080/v3/api-docs

Health check: http://localhost:8080/actuator/health

## API Reference

All endpoints are under `/api/v1/devices`.

### Create a device

```
POST /api/v1/devices
Content-Type: application/json

{
  "name": "iPhone 15",
  "brand": "Apple"
}
```

Both `name` and `brand` are required, max 255 characters, and must contain at least one letter or number (special characters allowed). The device is created with state `AVAILABLE`. Returns `201 Created`.

### Create multiple devices

```
POST /api/v1/devices/batch
Content-Type: application/json

{
  "devices": [
    {"name": "iPhone 15", "brand": "Apple"},
    {"name": "Galaxy S24", "brand": "Samsung"},
    {"name": "Pixel 8", "brand": "Google"}
  ]
}
```

Creates up to 100 devices in a single request. All devices are created with state `AVAILABLE`. Returns `201 Created` with a list of created devices.

### Get a device

```
GET /api/v1/devices/{id}
```

Returns `404` if the device does not exist.

### Get all devices

```
GET /api/v1/devices
```

Supports optional filtering and pagination. Filters can be combined:

| Parameter | Description                        | Example                              |
|-----------|------------------------------------|--------------------------------------|
| `brand`   | Filter by brand (case-insensitive) | `?brand=Apple`                       |
| `state`   | Filter by state                    | `?state=IN_USE`                      |
| `page`    | Page number (0-based)              | `?page=0`                            |
| `size`    | Page size (capped at 100)          | `?size=20`                           |
| `sort`    | Sort by field and direction        | `?sort=name,asc`                     |

Example combined filter: `GET /api/v1/devices?brand=Apple&state=AVAILABLE`

Valid states: `AVAILABLE`, `IN_USE`, `INACTIVE`

Sortable fields: `id`, `name`, `brand`, `state`, `createdAt`, `updatedAt`

### Update a device

```
PATCH /api/v1/devices/{id}
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "brand": "Apple",
  "state": "IN_USE"
}
```

All fields are optional. Omitted fields are left unchanged. If provided, `name` and `brand` must be non-empty, max 255 characters, and contain at least one letter or number (special characters allowed). Returns `404` if not found, `422` if trying to update `name` or `brand` while the device is `IN_USE`.

### Delete a device

```
DELETE /api/v1/devices/{id}
```

Returns `204` on success, `404` if not found, `422` if the device is `IN_USE`.

### Delete multiple devices

```
DELETE /api/v1/devices/batch
Content-Type: application/json

{
  "ids": [1, 2, 3]
}
```

Deletes up to 100 devices in a single request. The operation is all-or-nothing: if any device is not found or is `IN_USE`, no devices are deleted. Returns `204` on success, `404` if any ID is not found, `422` if any device is `IN_USE`.

## Domain Rules

- Devices are always created with state `AVAILABLE`.
- `name` and `brand` cannot be changed while a device is `IN_USE`. Only `state` can be updated.
- `IN_USE` devices cannot be deleted.
- Deletion is soft: the record is retained in the database with a `deleted_at` timestamp and is no longer returned by any API endpoint.
- `createdAt` is set automatically on creation and is immutable.
- `updatedAt` is set automatically on each update.

## Running Tests

Unit tests (no Docker required):

```bash
./gradlew test --tests "com.koval.devicemanager.unit.*"
```

Web layer tests (no Docker required):

```bash
./gradlew test --tests "com.koval.devicemanager.web.*"
```

Integration tests (requires Docker for Testcontainers):

```bash
./gradlew test --tests "com.koval.devicemanager.integration.*"
```

Full test suite:

```bash
./gradlew test
```

## Project Structure

```
src/
├── main/java/com/koval/devicemanager/
│   ├── api/
│   │   ├── controller/     # REST controllers
│   │   ├── dto/            # Request and response DTOs
│   │   └── exception/      # Global exception handler
│   ├── domain/
│   │   ├── model/          # Domain model (Device, DeviceState)
│   │   ├── repository/     # Repository interface (port)
│   │   ├── service/        # Business logic
│   │   └── exception/      # Domain exceptions
│   ├── infra/
│   │   ├── entity/         # JPA entities
│   │   ├── mapper/         # Domain <-> entity mapping
│   │   └── repository/     # Repository implementation (adapter)
│   └── config/             # OpenAPI configuration
└── main/resources/
    ├── application.yml
    └── db/migration/       # Flyway migrations
```
