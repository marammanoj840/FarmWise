# FarmWise Implementation Plan

## Phase 1 — Foundation & Tooling Alignment
1. Update `frontend/vite.config.ts` proxy to `:8080` and install `@tanstack/react-query`, `react-hook-form`, `@hookform/resolvers`, `zod`, `axios`.
2. Update `backend-spring/pom.xml` with JWT (`jjwt` or `nimbus-jose-jwt`), PDF generation (`openpdf`), and Apache Commons CSV / utility support.
3. Configure environment variable templates (root `.env.example`, `backend-spring/.env.example`, `frontend/.env.example`) with zero hardcoded secrets.
4. Design and apply complete normalized Flyway migrations (`V2`..`V6`) covering all 30+ tables and seed realistic Indian demo records cleanly flagged where applicable.

## Phase 2 — Backend Core, Auth & Ownership Security
1. Implement `JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService`, and role-based `SecurityConfig` for `FARMER`, `EXPERT`, `SUPPLIER`, `BUYER`, `ADMIN`.
2. Build `/api/v1/auth` (register, login, refresh, logout, forgot-password, reset-password, verify-email, me) and `/api/v1/users` (profile, password update, active sessions).
3. Build shared pagination, error handling, ownership guard, CSV/PDF export helpers, and audit log service.

## Phase 3 — Backend Domain Modules & Integrations
1. Farmer Core: `/api/v1/farms`, `/api/v1/crops`, `/api/v1/cultivation`, `/api/v1/inventory`, `/api/v1/finance` (with CSV/PDF report endpoints).
2. Intelligence: `/api/v1/weather` (Open-Meteo live lookup + graceful unavailable response), `/api/v1/soil` (soil records, crop knowledge base, crop observations), `/api/v1/assistant` (AI interface + farm/crop context + honest unconfigured fallback), `/api/v1/market-prices` (Agmarknet/eNAM interface + clearly labeled sample data).
3. Marketplace & Commerce: `/api/v1/products`, `/api/v1/marketplace`, `/api/v1/orders` (cart, checkout, order history, supplier fulfillment, payment-provider interface), `/api/v1/produce` (farmer produce listings & buyer inquiries).
4. Advisory, Notifications & Admin: `/api/v1/experts`, `/api/v1/consultations`, `/api/v1/notifications`, `/api/v1/admin`, `/api/v1/public` (contact inquiries, public highlights).

## Phase 4 — Frontend Design System & Public Website
1. Establish theme tokens (Light/Dark), typography, INR formatting, i18n scaffold, Axios client with token refresh, AuthContext, ToastContext, ErrorBoundary.
2. Build reusable UI primitives (`Button`, `Input`, `Select`, `Card`, `Badge`, `Modal`, `Drawer`, `ConfirmDialog`, `DataTable`, `Pagination`, `Skeleton`, `EmptyState`, `Breadcrumbs`, `Charts`).
3. Build WebGL-safe 3D hero (`FarmHero3D` + fallback) and all 12+ public pages (`Home`, `About`, `Features`, `HowItWorks`, `Solutions`, `Resources`, `Contact`, `FAQ`, `Privacy`, `Terms`, `Login`, `Register`, `ForgotPassword`, `ResetPassword`, `VerifyEmail`).

## Phase 5 — Authenticated Portals
1. Farmer Portal: Dashboard, Farms (list + detail), Crops, Cultivation, Inventory, Finance, Reports, Weather, Soil & Crop Intelligence, AI Assistant, Produce Listings, Market Prices, Experts, Marketplace/Orders.
2. Expert Portal: Profile/specializations, consultation requests, appointments, advisory responses.
3. Supplier Portal: Supplier storefront profile, product catalog management, stock & order fulfillment.
4. Buyer Portal: Produce discovery & inquiries, product marketplace, cart & order tracking.
5. Admin Portal: User/role governance, product & produce moderation, contact inquiries, platform analytics, audit logs, system health.

## Phase 6 — Quality, Verification & Deployment Readiness
1. Backend JUnit 5 + MockMvc tests covering auth, RBAC, ownership isolation, CRUD, validation, marketplace, finance, and integrations.
2. Frontend Vitest unit/component tests, TypeScript check (`tsc --noEmit`), ESLint, and production Vite build.
3. Verify Spring Boot against both H2 (`test` profile) and real PostgreSQL (`embedded-postgres`), plus HTTP smoke tests across frontend & backend.
4. Create root `docker-compose.yml`, `frontend/Dockerfile`, nginx config, and `docs/DEPLOYMENT.md`, `docs/API_OVERVIEW.md`.
