# FarmWise — Production Security Checklist

Complete this checklist prior to launching FarmWise publicly to guarantee compliance with modern cloud security standards.

---

## 1. Secrets & Credentials Hygiene
- [x] **No hardcoded secrets**: All database passwords, JWT secrets, and API keys are strictly configured via environment variables.
- [x] **Git repository clean**: No `.env`, `.env.local`, or credentials committed to version control (`.gitignore` verified).
- [ ] **Production JWT Secret**: Cryptographically random 64-character hex string generated via `openssl rand -hex 32`.
- [x] **Ephemeral secrets blocked**: In `application-prod.yml`, `allow-ephemeral-dev-secret` is hardcoded to `false`.
- [ ] **Database credentials rotated**: Cloud database password is distinct from local development passwords.

---

## 2. PostgreSQL & Database Security
- [x] **SSL Mode Enforced**: Connection string in production uses `?sslmode=require` or `?sslmode=verify-full`.
- [x] **Flyway Production Migration Rules**: `baseline-on-migrate` is `false`, `clean-disabled` is `true` in `application-prod.yml`.
- [x] **Demo Data Seeding Disabled**: `seed-demo-data: false` is locked in `application-prod.yml`.
- [ ] **Least Privilege DB User**: Database user has permissions limited to the `farmwise` database; superuser access is disabled.
- [ ] **Network Isolation**: PostgreSQL instance is accessible only from the backend container network (or via private VPC / IP allowlist).

---

## 3. Network, CORS & API Gateway Security
- [x] **CORS Origin Restriction**: CORS origins are restricted to verified domains via `CORS_ORIGINS` (no wildcards `*`).
- [x] **Rate Limiting Active**: `RateLimitFilter` active on all `/api/` paths (20 req/min on `/auth/`, 120 req/min on general endpoints).
- [x] **Forwarded Headers Configured**: `server.forward-headers-strategy: framework` enabled to correctly identify client IPs behind reverse proxies.
- [x] **Actuator Security**: Sensitive Actuator endpoints restricted; only `health`, `info`, and `metrics` exposed. Health details set to `never` in prod.
- [x] **Swagger UI / OpenAPI Disabled**: Swagger docs disabled in production by default (`OPENAPI_ENABLED=false`).

---

## 4. HTTP Security Headers
- [x] **HSTS (HTTP Strict Transport Security)**: Enabled with `max-age=31536000; includeSubDomains`.
- [x] **X-Frame-Options**: Set to `DENY` to prevent clickjacking.
- [x] **X-Content-Type-Options**: Set to `nosniff` to prevent MIME-type sniffing.
- [x] **Content-Security-Policy (CSP)**: Configured in `nginx.conf` and `vercel.json` / `netlify.toml`.
- [x] **Referrer-Policy**: Set to `strict-origin-when-cross-origin`.
- [x] **Permissions-Policy**: Restricted camera, microphone, and geolocation features.

---

## 5. File Upload & Storage Security
- [x] **File Size Limits**: Backend multipart limits enforced (`spring.servlet.multipart.max-file-size=10MB`).
- [x] **MIME Type Allowlist**: Restricted to safe image and document types (`image/jpeg`, `image/png`, `image/webp`, `application/pdf`).
- [x] **File Extension Validation**: Verified against extension allowlist (`.jpg`, `.jpeg`, `.png`, `.webp`, `.pdf`).
- [x] **Storage Key Isolation**: Keys generated using UUIDs and owner ID partitioning (`entity_type/user_id/random_uuid.ext`) to prevent path traversal.
- [x] **Access Control**: Users can only query or delete files they own; administrators have system-wide oversight.

---

## 6. Monitoring & Incident Response
- [x] **Structured Logging**: `logback-spring.xml` formats production logs as structured JSON for Datadog / CloudWatch / Papertrail ingestion.
- [x] **Health Probes**: Liveness and readiness probes active for container orchestrator health checks.
- [ ] **Alerting Configured**: Alerts configured for CPU > 80%, Memory > 85%, and HTTP 5xx rate > 1%.
- [x] **Automated Backups Verified**: Backup and disaster recovery procedures documented in `BACKUP_AND_RECOVERY.md`.
