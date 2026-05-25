# CouponManager

CouponManager is a small Spring Boot REST API for creating and redeeming discount coupons.

It focuses on a few core behaviors:

- unique coupon codes, normalized case-insensitively,
- maximum redemption limit per coupon,
- one redemption per user,
- optional country restriction based on client IP,
- pessimistic locking during redemption to avoid race conditions,
- structured error responses.

## Stack

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway dependency configured
- springdoc OpenAPI / Swagger UI
- Maven Wrapper
- Docker Compose
- JUnit 5 / Mockito / Spring MVC Test

## Project Layout

- `src/main/java` - application code
- `src/main/resources/application.yaml` - main configuration
- `src/main/resources/application-dev.yaml` - dev profile overrides
- `compose.yaml` - local PostgreSQL container

## API Overview

Base path: `/api/v1/coupons`

Endpoints:

- `POST /api/v1/coupons` - create a coupon
- `POST /api/v1/coupons/{code}/use` - redeem a coupon

OpenAPI:

- `GET /swagger-ui/index.html`
- `GET /v3/api-docs`
## Error Responses

Errors are returned in a structured format.

Example:
```json
 { "timestamp": "2026-05-25T12:00:00Z", "status": 409, "error": "Conflict", "code": "COUPON_LIMIT_REACHED", "message": "Coupon has reached maximum uses", "fieldErrors": {} }
```
Common error codes:

| HTTP Status | Code | Description |
|---|---|---|
| `400` | `VALIDATION_FAILED` | Request validation failed |
| `400` | `INVALID_REQUEST` | Invalid request data |
| `404` | `COUPON_NOT_FOUND` | Coupon does not exist |
| `409` | `COUPON_ALREADY_EXISTS` | Coupon code already exists |
| `409` | `COUPON_ALREADY_USED` | User has already used this coupon |
| `409` | `COUPON_LIMIT_REACHED` | Coupon usage limit has been reached |
| `409` | `COUPON_CURRENTLY_LOCKED` | Coupon is currently processed by another request |
| `403` | `COUPON_INVALID_COUNTRY` | Coupon cannot be used from detected country |
| `503` | `GEO_IP_LOOKUP_FAILED` | GeoIP provider failed or country could not be resolved |

---

## Security

Authentication and authorization are intentionally not implemented.

The recruitment task explicitly states that authentication is not required. Therefore, Spring Security is not used in this project.

Business protection is implemented at application and database level:

- coupon code uniqueness,
- usage limit protection,
- per-user usage uniqueness,
- pessimistic locking,
- validation,
- database constraints.


## Request / Response Model

### Create coupon

Request body:

```json
{
  "code": "WIOSNA",
  "targetCountry": "PL",
  "maxUses": 10
}
```

Response:

```json
{
  "id": "3f2f5af3-fb3b-4d8e-8b5d-8c0e3fb6c10f",
  "code": "WIOSNA"
}
```

### Use coupon

Request body:

```json
{
  "userId": "user-1"
}
```

Response:

```json
{
  "message": "Coupon applied successfully"
}
```

## Business Rules

- coupon code is normalized to uppercase,
- coupon code must be unique,
- `targetCountry` must be a 2-letter ISO code,
- `maxUses` must be greater than 0,
- the same user cannot redeem the same coupon twice,
- coupon cannot be redeemed after the usage limit is reached,
- when country restriction is enabled, the user country must match the coupon country,
- user country is resolved from `X-Forwarded-For` or the request remote address.

## Prerequisites

Required:

- Java 21
- Docker Desktop or another Docker runtime

Optional but useful:

- `curl`
- Postman / Bruno / Insomnia

Note: this workspace is currently running Java 17, and Maven compilation fails with `release version 21 not supported`. Use Java 21 before running the project.

## Database

The app is configured for PostgreSQL on:

- host: `localhost`
- port: `5432`
- database: `coupon_manager`
- user: `coupon_manager`
- password: `coupon_manager`

These values come from [compose.yaml](/D:/projects/private/CouponManager/compose.yaml) and [application.yaml](/D:/projects/private/CouponManager/src/main/resources/application.yaml).

### Start PostgreSQL

```bash
docker compose up -d
```

### Important schema note

The project has Flyway on the classpath, but there are currently no migration files under `src/main/resources/db/migration`. The app also uses `spring.jpa.hibernate.ddl-auto=validate`, so the schema must already exist before startup.

If you want to run the current code as-is, initialize the schema manually:

```sql
CREATE TABLE coupons (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    max_uses INTEGER NOT NULL,
    current_uses INTEGER NOT NULL,
    target_country VARCHAR(2) NOT NULL,
    CONSTRAINT uk_coupons_code UNIQUE (code)
);

CREATE TABLE coupon_usages (
    id BIGSERIAL PRIMARY KEY,
    coupon_id UUID NOT NULL,
    user_id VARCHAR(200) NOT NULL,
    used_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_coupon_usages_coupon
        FOREIGN KEY (coupon_id) REFERENCES coupons (id),
    CONSTRAINT uk_coupon_usages_coupon_id_user_id
        UNIQUE (coupon_id, user_id)
);
```

Example:

```bash
docker exec -i coupon-manager-postgres psql -U coupon_manager -d coupon_manager
```

Then paste the SQL above.

## Running the Application

### Default profile

Runs on port `8080`:

```bash
./mvnw.cmd spring-boot:run
```

### Dev profile

Runs on port `9090` because of `application-dev.yaml`:

```bash
./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### Disable country restriction for local testing

This is useful if you do not want the app to call the GeoIP provider while testing from localhost:

```bash
./mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--coupon-manager.redemption.country-restriction.enabled=false"
```

You can combine it with the dev profile if needed.

## Configuration

Main application properties:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/coupon_manager
    username: coupon_manager
    password: coupon_manager

coupon-manager:
  redemption:
    country-restriction:
      enabled: true
  integrations:
    geo-ip:
      provider: ip-api
      base-url: http://ip-api.com
```

Notes:

- with country restriction enabled, redemption triggers an outbound call to `ip-api.com`,
- for predictable local testing, either send an `X-Forwarded-For` header with a public IP or disable country restriction,
- the app reads the first IP from `X-Forwarded-For`.

## HTTP Requests For Testing

Examples below assume the app runs on `http://localhost:8080`.

### 1. Create a coupon

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons -H "Content-Type: application/json" -d "{\"code\":\"wiosna\",\"targetCountry\":\"PL\",\"maxUses\":2}"
```

Expected result:

- HTTP `201 Created`
- `Location: /api/v1/coupons/WIOSNA`

### 2. Redeem a coupon with country restriction disabled

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons/WIOSNA/use -H "Content-Type: application/json" -d "{\"userId\":\"user-1\"}"
```

### 3. Redeem a coupon with country restriction enabled

Use a Polish IP in `X-Forwarded-For` for a coupon targeted to `PL`:

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons/WIOSNA/use -H "Content-Type: application/json" -H "X-Forwarded-For: 83.1.2.3" -d "{\"userId\":\"user-1\"}"
```

### 4. Try redeeming the same coupon twice with the same user

First request:

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons/WIOSNA/use -H "Content-Type: application/json" -d "{\"userId\":\"user-2\"}"
```

Second request with the same `userId`:

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons/WIOSNA/use -H "Content-Type: application/json" -d "{\"userId\":\"user-2\"}"
```

Expected error:

```json
{
  "timestamp": "2026-05-25T09:00:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "COUPON_ALREADY_USED",
  "message": "User user-2 already used this coupon",
  "fieldErrors": {}
}
```

### 5. Exceed the coupon usage limit

Create a coupon with `maxUses=1`, redeem it once, then call the same endpoint again with a different user:

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons/LIMIT1/use -H "Content-Type: application/json" -d "{\"userId\":\"user-a\"}"
```

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons/LIMIT1/use -H "Content-Type: application/json" -d "{\"userId\":\"user-b\"}"
```

Expected error code: `COUPON_LIMIT_REACHED`

### 6. Use an unknown coupon

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons/UNKNOWN/use -H "Content-Type: application/json" -d "{\"userId\":\"user-1\"}"
```

Expected error code: `COUPON_NOT_FOUND`

### 7. Send invalid create payload

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons -H "Content-Type: application/json" -d "{\"code\":\" \",\"targetCountry\":\"POLAND\",\"maxUses\":0}"
```

Expected error:

```json
{
  "timestamp": "2026-05-25T09:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "fieldErrors": {
    "code": "must not be blank",
    "targetCountry": "Country must be a two-letter ISO country code",
    "maxUses": "must be greater than or equal to 1"
  }
}
```

### 8. Country mismatch example

For a coupon targeted to `PL`, send a non-Polish IP:

```bash
curl -i -X POST http://localhost:8080/api/v1/coupons/WIOSNA/use -H "Content-Type: application/json" -H "X-Forwarded-For: 8.8.8.8" -d "{\"userId\":\"user-3\"}"
```

Expected error code: `COUPON_INVALID_COUNTRY`

## PowerShell Examples

If you want examples without shell escaping issues on Windows PowerShell:

```powershell
$body = @{
  code = "wiosna"
  targetCountry = "PL"
  maxUses = 2
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/coupons" `
  -ContentType "application/json" `
  -Body $body
```

```powershell
$body = @{
  userId = "user-1"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/coupons/WIOSNA/use" `
  -ContentType "application/json" `
  -Headers @{ "X-Forwarded-For" = "83.1.2.3" } `
  -Body $body
```

## Running Tests

```bash
./mvnw.cmd test
```

Before running tests locally, make sure Java 21 is active. In this environment, the command currently fails on Java 17 with:

```text
error: release version 21 not supported
```
