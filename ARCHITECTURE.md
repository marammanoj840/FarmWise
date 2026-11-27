# FarmWise Architecture

## 1. System Overview

FarmWise is a modular full-stack agricultural technology platform built for Indian farmers and agribusiness stakeholders (`FARMER`, `EXPERT`, `SUPPLIER`, `BUYER`, `ADMIN`), defaulting to INR (`₹`) currency and structured for future Indian regional language localization and multi-currency support.

```
+-----------------------+        HTTPS / REST JSON        +-------------------------------+
|  Frontend SPA         |  <--------------------------->  |  Spring Boot 4.2 Backend      |
|  React 19 + TS + Vite |    /api/v1/* + Bearer JWT       |  Java 21 + Spring Security    |
|  Tailwind + R3F + RQ  |                                 |  Controller -> Service -> JPA |
+-----------------------+                                 +---------------+---------------+
                                                                          |
                       +--------------------------------------------------+------------------+
                       |                                                  |                  |
                       v                                                  v                  v
          +-------------------------+                        +--------------------+  +-----------------+
          |  PostgreSQL 16+         |                        | External Providers |  | Legacy / ML Ref |
          |  Flyway Migrations      |                        | (Interface-driven) |  | backend/ & ml/  |
          |  30+ Normalized Tables  |                        | - Open-Meteo       |  | (Preserved)     |
          +-------------------------+                        | - AI / Market /    |  +-----------------+
                                                             |   Email / SMS / S3 |
                                                             |   / Maps / Payment |
                                                             +--------------------+
```

## 2. Backend Architecture (`backend-spring/`)

### Layered Package Structure
Under `com.farmwise.backend`:
- `config/` — CORS, OpenAPI/Swagger, UTC JPA auditing, RestClient, integration properties
- `security/` — Stateless JWT authentication (`JwtService`, `JwtAuthenticationFilter`), `CustomUserDetailsService`, `SecurityConfig`, `BCryptPasswordEncoder`, JSON 401/403 handlers, resource ownership verification
- `common/` — Shared base audit entity, standardized paginated/success DTOs, CSV/PDF export utilities, validation helpers
- `auth/` — Registration, login, JWT access + rotating refresh tokens, logout/session revocation, forgot/reset password, email verification
- `user/` — User profiles, preferences, role assignment, session management
- `farm/` — Farms, farm locations, area units, soil/irrigation metadata, photo URLs
- `crop/` — Crop catalog, varieties, farm crop assignments, growth stages, field observations, harvest logs
- `cultivation/` — Cultivation activities (9 lifecycle stages), scheduled tasks, reminders
- `inventory/` — Agricultural inventory items (seeds, fertilizers, pesticides, equipment, irrigation), purchase/usage transactions, low-stock alerts
- `finance/` — Expenses, income, categories, crop/farm financial summaries, P&L estimates, CSV/PDF export
- `weather/` — `WeatherProvider` interface + `OpenMeteoWeatherProvider` (real HTTP integration with graceful unavailability handling) + cached/recorded `weather_records` & agricultural advisories
- `soil/` — Soil test records, NPK/pH fertility observations, extensible crop knowledge base, crop health & pest/disease observations
- `assistant/` — Provider-independent `AiAssistantProvider` interface, authorized farm/crop context injection, conversation history, honest unconfigured fallback with deterministic agronomic guidance
- `marketplace/` — Product categories, supplier profiles, products, shopping cart, orders, `PaymentProvider` abstraction, farmer produce listings & buyer inquiries
- `marketprice/` — `MarketPriceProvider` interface (Agmarknet/eNAM adapter architecture), clearly labeled sample reference records when live feed credentials are absent, historical trend charts
- `expert/` — Expert directory, specializations, verification flags, consultation requests, appointments, reviews
- `notification/` — In-app notifications, preferences, `EmailNotificationProvider` & `SmsNotificationProvider` interfaces
- `admin/` — User/role management, marketplace/produce moderation, platform analytics, contact inquiries, audit logs, system health overview

### Database Architecture
- PostgreSQL primary database managed strictly via **Flyway** (`classpath:db/migration/V1..Vn`).
- Hibernate runs with `ddl-auto: validate` to guarantee entity-to-migration parity.
- Normalized UUID primary keys, UTC timestamps (`created_at`, `updated_at`), foreign keys, check constraints, unique constraints, and B-tree indexes on ownership (`farmer_id`, `user_id`), status, and lookup columns.
- Profiles: `dev` (PostgreSQL), `test` (H2 PostgreSQL compatibility mode with Flyway), `prod` (strict env-var datasource, OpenAPI disabled by default).

### Security & Ownership Controls
- Passwords hashed with BCrypt (strength 12).
- Short-lived JWT access tokens + database-tracked refresh tokens with rotation & revocation.
- Method-level & route-level RBAC (`FARMER`, `EXPERT`, `SUPPLIER`, `BUYER`, `ADMIN`) enforced server-side.
- Service-layer ownership checks ensure farmers only read/mutate their own farms, crops, cultivation logs, inventory, finances, soil records, and AI conversations.
- All sensitive admin and security actions persist to `audit_logs`.

## 3. Frontend Architecture (`frontend/`)

- **Routing**: React Router v7 with public routes, auth routes, and role-protected `/app/*` portal routes.
- **Data Fetching & State**: TanStack Query for server caching/invalidation + React Context for auth session, theme (`light`/`dark`), localization/currency scaffold, and toast notifications.
- **Forms & Validation**: React Hook Form + Zod schemas matching backend Bean Validation contracts.
- **HTTP Layer**: Centralized Axios client with JWT access-token injection and automatic single-flight refresh-token retry on `401`.
- **3D & Motion**: Three.js + React Three Fiber hero scene with WebGL capability detection, mobile pixel-ratio capping, `prefers-reduced-motion` awareness, and an illustrated SVG fallback. GSAP/Motion/Lenis applied selectively for smooth transitions without obstructing core tasks.

## 4. Integration Interfaces & Configuration Status

| Integration | Interface | Active Implementation | External Credentials Needed? |
|---|---|---|---|
| Weather | `WeatherProvider` | `OpenMeteoWeatherProvider` (live Open-Meteo REST API) | Optional (no key required for Open-Meteo; `WEATHER_PROVIDER`) |
| AI Assistant | `AiAssistantProvider` | Configurable HTTP provider + honest unconfigured agronomic fallback | Yes (`AI_PROVIDER_API_KEY`, `AI_PROVIDER_URL`, `AI_MODEL`) |
| Market Prices | `MarketPriceProvider` | Agmarknet/eNAM adapter interface + clearly labeled sample data fallback | Yes (`AGMARKNET_API_KEY`, `ENAM_API_KEY`) |
| Payments | `PaymentProvider` | `ManualPendingPaymentProvider` (no fake capture) | Yes (`PAYMENT_PROVIDER`, `PAYMENT_KEY_ID`, `PAYMENT_KEY_SECRET`) |
| Email | `EmailNotificationProvider` | `LoggingEmailProvider` (SMTP interface ready) | Yes (`SMTP_HOST`, `SMTP_USER`, `SMTP_PASSWORD`) |
| SMS / OTP | `SmsNotificationProvider` | `LoggingSmsProvider` (SMS gateway interface ready) | Yes (`SMS_PROVIDER`, `SMS_API_KEY`) |
| Maps / Geo | `GeocodingProvider` | Coordinates + Open-Meteo geocoding / Maps adapter | Optional (`MAPS_API_KEY`) |
| File Storage | `FileStorageProvider` | Local metadata / S3-compatible interface | Optional (`S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`) |
