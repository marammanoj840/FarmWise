# FarmWise — Complete Agricultural Technology Platform

**Smarter Farming. Better Harvests. Stronger Futures.**

FarmWise is a full-stack agricultural technology ecosystem built for Indian farmers and agricultural stakeholders (`FARMER`, `EXPERT`, `SUPPLIER`, `BUYER`, `ADMIN`), defaulting to INR (`₹`) currency with a localization-ready UI architecture.

## Core Modules

- **Public Website**: Home (with WebGL 3D farm scene & SVG fallback), About, Features, How It Works, Solutions, Farmer Resources, Contact, FAQ, Privacy Policy, Terms, and Authentication flows.
- **Farmer Portal**: Operations Dashboard, Multi-Farm Management, Crop & Variety Lifecycle, 9-Stage Cultivation & Tasks, Inventory & Low-Stock Alerts, INR Finance & P&L, PDF/CSV Reports, Open-Meteo Weather, Soil & Crop Intelligence, AI Assistant Interface, Produce Sales, Market Price Intelligence, and Expert Advisory.
- **Role Portals**: Agricultural Expert Workspace, Input Supplier Storefront & Fulfillment, Produce Buyer Procurement, and Platform Administration (User/Role Governance, Moderation, Audit Logs, System Health).

## Tech Stack

- **Frontend (`frontend/`)**: React 19, TypeScript, Vite, Tailwind CSS v4, React Router v7, TanStack Query, React Hook Form, Zod, Axios, Three.js / React Three Fiber, Motion, Lenis, Recharts, Lucide Icons, Vitest.
- **Backend (`backend-spring/`)**: Java 21 LTS, Spring Boot 4.2, Maven, Spring Web, Spring Data JPA, Spring Security, Nimbus JOSE JWT + rotating refresh tokens, Bean Validation, PostgreSQL, Flyway migrations (`V1`–`V7`), OpenPDF, Apache Commons CSV, SpringDoc OpenAPI, Actuator, JUnit 5.
- **Legacy / ML Reference (`backend/`, `ml/`)**: Preserved Python/Scikit-learn crop recommendation scripts and early schema drafts.

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md)
- [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md)
- [PROJECT_STATUS.md](PROJECT_STATUS.md)
- [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)
- [docs/API_OVERVIEW.md](docs/API_OVERVIEW.md)

## Quick Start

1. Copy `.env.example` to `.env` and set strong local credentials.
2. **Docker Compose**:
   ```bash
   docker compose up --build
   ```
3. **Or Manual Local Run** (see [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for JDK 21 and PostgreSQL setup):
   - Backend: `cd backend-spring && mvn spring-boot:run`
   - Frontend: `cd frontend && npm install && npm run dev`
