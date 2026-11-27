# FarmWise Deployment & Operations Guide

## 1. Prerequisites

- **Docker & Docker Compose** (recommended for full-stack container run), OR:
  - **Java 21 LTS** + **Apache Maven 3.9+**
  - **Node.js 20+** + **npm 10+**
  - **PostgreSQL 16+**

## 2. Environment Configuration

Copy `.env.example` to `.env` in the repository root before starting:

```bash
cp .env.example .env
```

Required variables for **production** (`SPRING_PROFILES_ACTIVE=prod`):
- `SPRING_DATASOURCE_URL` — JDBC PostgreSQL URL
- `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET` — Cryptographically random string of **at least 32 characters** (startup fails intentionally in `prod` if missing)
- `CORS_ORIGINS` — Comma-separated production frontend URLs

Optional External Integration Credentials (the application stays fully runnable when unset):
- `WEATHER_PROVIDER=open-meteo` (works without an API key; falls back transparently if offline)
- `AI_PROVIDER`, `AI_PROVIDER_URL`, `AI_PROVIDER_API_KEY`, `AI_MODEL` (falls back to non-AI agronomic reference mode)
- `MARKET_DATA_PROVIDER`, `AGMARKNET_API_URL`, `AGMARKNET_API_KEY` (displays clearly labeled sample mandi benchmarks when unset)
- `PAYMENT_PROVIDER`, `PAYMENT_KEY_ID`, `PAYMENT_KEY_SECRET` (orders record in `PENDING_PAYMENT_CONFIG` without processing payments until configured)
- `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` / `SMS_API_KEY` (logs email/SMS dispatches cleanly until configured)

## 3. Running with Docker Compose

```bash
docker compose up --build -d
```

Services started:
- PostgreSQL 16 (`:5432`)
- Spring Boot 4.2 API (`:8080`, health at `/api/v1/health` and `/actuator/health`)
- Nginx-served React SPA (`:5173`, proxying `/api/*` to `backend:8080`)

## 4. Running Locally Without Docker

### Backend (Java 21 + Spring Boot)
```powershell
cd backend-spring
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/farmwise"
$env:SPRING_DATASOURCE_USERNAME = "farmwise"
$env:SPRING_DATASOURCE_PASSWORD = "<your-db-password>"
mvn spring-boot:run
```

### Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```

## 5. Health Checks & OpenAPI

- Application & DB Health: `GET /api/v1/health`
- Actuator Liveness/Readiness: `GET /actuator/health`
- OpenAPI Specification (dev): `GET /v3/api-docs`
- Swagger UI (dev): `GET /swagger-ui.html` (disabled automatically in `prod` unless `OPENAPI_ENABLED=true`)
