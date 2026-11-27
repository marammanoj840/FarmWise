# FarmWise — Production Environment Guide

This document defines the complete environment specification for the FarmWise production deployment across all tiers: React Frontend, Spring Boot Backend, PostgreSQL Database, and Object Storage.

---

## 1. Environment Topology

```
                  [ Users & Clients ]
                           │ HTTPS (443)
                  [ Cloudflare Edge / CDN ]
                (WAF, DDoS, SSL/TLS, Caching)
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
    [ Frontend Host ]           [ Backend API Host ]
 (Vercel / Cloudflare Pages /   (Render / Railway / AWS ECS /
  Static S3+CloudFront)          Cloud Run / Docker Host)
             │                           │
      Static SPA Assets           Spring Boot 4.2 (Port 8080)
                                         │
                   ┌─────────────────────┼─────────────────────┐
                   ▼                     ▼                     ▼
         [ PostgreSQL 16/18 ]   [ S3 / Cloudflare R2 ]  [ External APIs ]
         (Managed DB with SSL)  (Media & Documents)   (Open-Meteo, AI, etc.)
```

---

## 2. Production Environment Variables Reference

### Spring Boot Backend (`backend-spring`)

| Variable | Required | Default / Example | Purpose |
| :--- | :---: | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | **YES** | `prod` | Activates production security, disables demo data seeding, hides Swagger UI unless opted in |
| `SERVER_PORT` | No | `8080` (or injected by PaaS via `$PORT`) | Port on which the Spring Boot Tomcat container listens |
| `SPRING_DATASOURCE_URL` | **YES** | `jdbc:postgresql://<host>:5432/<db>?sslmode=require` | JDBC connection string to managed PostgreSQL instance. Must require SSL. |
| `SPRING_DATASOURCE_USERNAME` | **YES** | `farmwise_prod` | Dedicated production database user with least privilege |
| `SPRING_DATASOURCE_PASSWORD` | **YES** | *(Secure secret)* | Database user password (32+ random characters) |
| `DB_POOL_MAX_SIZE` | No | `20` | HikariCP maximum connection pool size |
| `DB_POOL_MIN_IDLE` | No | `5` | HikariCP minimum idle connections |
| `JWT_SECRET` | **YES** | *(64-byte random string)* | HS256 JWT signing secret. Ephemeral secrets are rejected in `prod`. |
| `JWT_ACCESS_TOKEN_MINUTES` | No | `15` | Lifetime of short-lived JWT access tokens |
| `JWT_REFRESH_TOKEN_DAYS` | No | `7` | Lifetime of rotating refresh tokens |
| `JWT_ISSUER` | No | `farmwise-api` | JWT token issuer claim |
| `CORS_ORIGINS` | **YES** | `https://farmwise.ag,https://www.farmwise.ag` | Comma-separated list of allowed browser frontend origins |
| `RATE_LIMIT_AUTH_RPM` | No | `20` | Maximum requests per minute per IP for authentication endpoints (`/api/v1/auth/**`) |
| `RATE_LIMIT_API_RPM` | No | `120` | Maximum requests per minute per IP for general API endpoints (`/api/v1/**`) |
| `STORAGE_PROVIDER` | No | `s3` or `local-metadata` | Storage driver (`s3` for AWS S3 / Cloudflare R2 / MinIO) |
| `S3_BUCKET` | Cond. | `farmwise-media` | Target bucket name |
| `S3_REGION` | Cond. | `auto` (R2) or `ap-south-1` (AWS Mumbai) | Cloud region for object storage |
| `S3_ENDPOINT` | Cond. | `https://<account_id>.r2.cloudflarestorage.com` | Custom endpoint for Cloudflare R2 or MinIO |
| `S3_ACCESS_KEY` | Cond. | *(Secret)* | S3 access key ID |
| `S3_SECRET_KEY` | Cond. | *(Secret)* | S3 secret access key |
| `S3_PUBLIC_BASE_URL` | No | `https://media.farmwise.ag` | CDN/public URL prefix for uploaded media assets |
| `STORAGE_MAX_FILE_SIZE_BYTES`| No | `10485760` (10 MB) | Maximum permitted upload file size |
| `OPENAPI_ENABLED` | No | `false` | Set to `true` only if Swagger UI documentation is required in production |
| `WEATHER_PROVIDER` | No | `open-meteo` | Weather data provider (`open-meteo` requires no API key) |
| `WEATHER_API_BASE_URL` | No | `https://api.open-meteo.com/v1` | Open-Meteo weather API endpoint |
| `GEOCODING_API_BASE_URL` | No | `https://geocoding-api.open-meteo.com/v1` | Open-Meteo geocoding endpoint |
| `AI_PROVIDER` | No | `none` or `gemini` / `openai` | AI assistant provider |
| `AI_PROVIDER_API_KEY` | Cond. | *(Secret)* | API key for agricultural AI recommendations |
| `PAYMENT_PROVIDER` | No | `unconfigured` or `razorpay` | Payment gateway integration |
| `PAYMENT_KEY_ID` | Cond. | *(Secret)* | Razorpay / Payment Key ID |
| `PAYMENT_KEY_SECRET` | Cond. | *(Secret)* | Razorpay / Payment Key Secret |
| `SMTP_HOST` | No | `smtp.sendgrid.net` | Outbound email relay |
| `SMTP_PORT` | No | `587` | Outbound SMTP port |
| `SMTP_FROM` | No | `noreply@farmwise.ag` | Verified sender email address |

---

### React Frontend (`frontend`)

| Variable | Required | Production Value | Purpose |
| :--- | :---: | :--- | :--- |
| `VITE_API_BASE_URL` | **YES** | `https://api.farmwise.ag/api/v1` | Backend REST API endpoint URL |

---

## 3. Secret Generation Utility Commands

Before launching in production, generate cryptographically secure secrets:

```bash
# Generate 64-character hex string for JWT_SECRET
openssl rand -hex 32

# Generate 32-character database password
openssl rand -base64 24
```

---

## 4. Verification & Health Probes

- **Liveness Probe**: `GET /api/v1/health` or `/actuator/health/liveness` (returns HTTP 200 with JSON status `UP`)
- **Readiness Probe**: `GET /actuator/health/readiness` (confirms PostgreSQL and internal components are ready)
- **Metrics Probe**: `GET /actuator/metrics` or `/actuator/prometheus` (for Prometheus/Grafana monitoring)
