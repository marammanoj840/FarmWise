# FarmWise Project Status

> Last updated: 2026-11-27

## Repository Inspection Summary

### 1. Frontend (`frontend/`)
- **Stack**: React 19.3.1, TypeScript 6.0.3, Vite 8.4.0, Tailwind CSS 4.3.5 (`@tailwindcss/vite`), React Router DOM 7.18.7, TanStack Query 5, React Hook Form 7, Zod 4, Axios, Recharts 3.11.1, Lucide React, Three.js / `@react-three/fiber` / `@react-three/drei`, Motion, Lenis, Vitest 5.1.2, Testing Library.
- **Completed**: Full design system (light/dark modes, INR ₹ formatting, i18n scaffold), reusable UI component library, WebGL 3D agricultural hero with mobile optimization & SVG fallback, 16 public & auth pages, and complete role-based portals (`FARMER`, `EXPERT`, `SUPPLIER`, `BUYER`, `ADMIN`).

### 2. Primary Backend (`backend-spring/`)
- **Stack**: Java 21 LTS, Spring Boot 4.2.0, Maven 3.9.6, Spring Web, Spring Data JPA, Spring Security, Nimbus JOSE JWT + rotating SHA-256 refresh tokens, Bean Validation, PostgreSQL, Flyway (`V1`–`V7`), OpenPDF, Apache Commons CSV, SpringDoc OpenAPI, Actuator, JUnit 5.
- **Completed**: 173 Java source files implementing all `/api/v1/*` domain APIs, strict farm ownership isolation, server-side RBAC, audit logging, Open-Meteo weather integration, Soil & Crop Intelligence, AI Assistant provider abstraction, Marketplace + Produce Commerce + Payment provider abstraction, Market Price Intelligence adapter, Expert Advisory, Notifications, and Admin Governance.

### 3. Legacy Python / ML Folders (`backend/`, `ml/`)
- **Status**: Preserved intact for reference and ML dataset/script continuity (`backend/.venv` also includes `embedded-postgres` 18.6.3 used for local real-PostgreSQL verification).

---

## Phase Progress Log

| Phase | Scope | Status | Verification Evidence |
|---|---|---|---|
| Inspection | Repo & environment audit | Done | Verified Node 24, JDK 21, Maven 3.9.6, initial baseline files. |
| Phase 1 | Foundation, docs, DB schema, security core | Done | Flyway `V1`–`V7` validated on H2 and PostgreSQL 18; `mvn clean package` succeeded. |
| Phase 2 | Public Website & Design System | Done | Public pages, Light/Dark themes, WebGL 3D hero + SVG fallback built cleanly. |
| Phase 3 | Authentication & User Management | Done | Register, Login, JWT + refresh rotation, password reset, email verification, profile/sessions tested (`AuthIntegrationTest`). |
| Phase 4 | Farmer Portal | Done | Dashboard, Farms, Crops, Cultivation, Inventory, Finance, PDF/CSV Reports tested (`FarmPlatformDomainTest`). |
| Phase 5 | Intelligence Modules | Done | Live Open-Meteo verified; Soil records, Crop Knowledge, AI fallback, Sample-labeled Market Prices verified. |
| Phase 6 | Marketplace & Produce Commerce | Done | Categories, products, cart, checkout (`PENDING_PAYMENT_CONFIG`), produce listings, buyer inquiries verified. |
| Phase 7 | Experts, Notifications & Admin | Done | Expert verification, consultations, appointments, notifications, admin moderation, audit logs tested (`SecurityAndOwnershipTest`). |
| Phase 8 | Testing, Docker & Deployment Docs | Done | Backend: 10/10 tests pass + JAR built; Frontend: `tsc`, `eslint`, 4/4 Vitest, `vite build` pass; Real PostgreSQL 18 E2E smoke test passed; Dockerfiles & `docker-compose.yml` added. |
| Phase 9 | Cloud Deployment & Public Launch Hardening | Done | Managed PostgreSQL configuration, connection pooling (`HikariCP`), Flyway `V8` migration, rate limiting (`RateLimitFilter`), S3-compatible storage (`StorageService`), security headers (HSTS, CSP, X-Frame-Options), SPA hosting configs (`vercel.json`, `netlify.toml`, `_redirects`), GitHub Actions CI/CD workflows (`ci.yml`, `deploy-prepare.yml`), Docker compose production profile (`docker-compose.prod.yml`), and complete deployment documentation (`CLOUD_DEPLOYMENT_GUIDE.md`, `PRODUCTION_ENVIRONMENT.md`, `DOMAIN_SETUP.md`, `BACKUP_AND_RECOVERY.md`, `PRODUCTION_SECURITY_CHECKLIST.md`). 23/23 backend tests pass. |

## Known Environment / Credential Notes
- `docker` CLI is not installed on the local Windows PATH, so Docker image builds (`docker compose up --build`) were prepared and checked syntactically rather than executed locally; container execution should be run in an environment with Docker Engine installed or via GitHub Actions.
- External optional credentials (`AI_PROVIDER_API_KEY`, `AGMARKNET_API_KEY`, `PAYMENT_KEY_ID`/`SECRET`, `SMTP_HOST`, `SMS_API_KEY`, `S3_BUCKET`, `MAPS_API_KEY`) are intentionally unconfigured in `.env.example` and degrade gracefully with transparent UI/API states.
- Public deployment is strictly gated: deployment workflows require manual trigger and confirmation token to prevent accidental exposure of production infrastructure.
