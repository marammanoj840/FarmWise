# FarmWise Backend (Java 21 + Spring Boot 4.2)

Modular Spring Boot backend foundation for the FarmWise agricultural ecosystem.

## Architecture

Base package: `com.farmwise.backend`

- `config/` — CORS properties, OpenAPI/Swagger configuration, UTC JPA auditing
- `controller/` — REST endpoints (`/api/v1/health`)
- `service/` — Application services & live JDBC database health check
- `repository/` — Spring Data JPA repositories (`RoleRepository`)
- `entity/` — JPA entities & audit base class (`BaseAuditEntity`, `RoleEntity`)
- `dto/` — Request/response DTO records (`HealthResponse`, `ApiErrorResponse`)
- `security/` — Stateless Spring Security filter chain, BCrypt `PasswordEncoder`, JSON 401/403 handlers
- `exception/` — Global exception & Bean Validation error handling (`GlobalExceptionHandler`)

## Prerequisites

- **Java 21 LTS** (e.g., Eclipse Temurin 21)
- **Apache Maven 3.9+**
- **PostgreSQL 16+** (local install or Docker)

> **Windows note:** If your default `JAVA_HOME` points to JDK 17, point `JAVA_HOME` to JDK 21 before running Maven:
> ```powershell
> $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
> $env:Path = "$env:JAVA_HOME\bin;$env:Path"
> ```

## Configuration Profiles

- `dev` (`application-dev.yml`, default): Connects to PostgreSQL via `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
- `test` (`application-test.yml`): Uses in-memory H2 in PostgreSQL compatibility mode with Flyway + Hibernate `ddl-auto: validate` for automated tests.
- `prod` (`application-prod.yml`): Requires `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` with no defaults, hides stacktraces, and disables Swagger UI unless `OPENAPI_ENABLED=true`.

## Running Locally

### Option 1: Docker Compose (Backend + PostgreSQL)

1. Copy `.env.example` to `.env` and set strong local credentials.
2. Start services:
   ```bash
   docker compose up --build
   ```

### Option 2: Maven with Local PostgreSQL

1. Copy `.env.example` to `.env` and ensure a PostgreSQL database named `farmwise` exists.
2. Export `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
3. Build and run:
   ```bash
   mvn clean verify
   mvn spring-boot:run
   ```

## Key Foundation Endpoints

- Health Check: `GET /api/v1/health`
- Spring Boot Actuator Health: `GET /actuator/health`
- OpenAPI Spec (dev): `GET /v3/api-docs`
- Swagger UI (dev): `GET /swagger-ui.html`
