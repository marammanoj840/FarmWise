# FarmWise — Cloud Deployment & Infrastructure Status Report

**Date**: 2026-11-28  
**Repository**: [https://github.com/marammanoj840/FarmWise](https://github.com/marammanoj840/FarmWise) (Public)  
**Deployment Pipeline**: GitHub Actions + Docker Multi-stage Containers  
**Target Hosts**: Cloudflare Pages (Frontend) + Render Web Service (Backend) + Managed PostgreSQL (Database)

---

## 1. Cloud Deployment Readiness Status

| Tier | Target Cloud Provider | Local Preparation Status | Remote Deployment Status | Required Next Action |
| :--- | :--- | :---: | :---: | :--- |
| **Frontend** | **Cloudflare Pages** | **100% COMPLETE** | **READY TO LINK** | Connect GitHub repo in Cloudflare Dashboard. |
| **Backend** | **Render Web Service** | **100% COMPLETE** | **READY TO LINK** | Connect GitHub repo as Docker Web Service in Render. |
| **Database** | **Render / Neon PostgreSQL** | **100% COMPLETE** | **READY TO PROVISION** | Create managed PostgreSQL instance and inject DB URL. |
| **Storage** | **Cloudflare R2 / AWS S3** | **100% COMPLETE** | **READY FOR KEYS** | Supply bucket name and S3 API token. |
| **DNS / SSL** | **Cloudflare DNS** | **100% COMPLETE** | **READY FOR DOMAIN** | Point custom domain nameservers to Cloudflare. |

---

## 2. Infrastructure Architecture & Verified Operating Costs

As verified through live cloud pricing audits:

```
[ Frontend: Cloudflare Pages ]           [ Backend: Render Web Service ]
- Build: npm run build                  - Multi-stage Dockerfile
- Output: dist                          - Java 21 LTS + Spring Boot 4.2
- Unlimited bandwidth                   - Auto-scaling & Health Checks
- Free Tier: $0.00 / month               - Starter: $7.00 / month (or Standard: $25.00)
             │                                       │
             ▼                                       ▼
[ Edge CDN & WAF: Cloudflare ]          [ Database: Render Managed PostgreSQL ]
- Full (Strict) SSL/TLS                 - Automated daily snapshots & PITR
- DDoS mitigation                       - HikariCP connection pooling
- Free Tier: $0.00 / month               - Basic (1GB RAM): $19.00 / month
                                                     │
                                                     ▼
                                        [ Storage: Cloudflare R2 ]
                                        - Zero egress bandwidth fees
                                        - 10 GB free monthly: $0.00 – $2.00 / month
```

**Total Estimated Monthly Operating Cost**: **~$26.00 to $51.00 USD / month**.

---

## 3. Deployment Artifacts Prepared in Codebase

1. **Docker Container Configuration**:
   - [backend-spring/Dockerfile](file:///C:/Users/maram/Desktop/FarmWise/backend-spring/Dockerfile): Multi-stage build (`maven:3.9.9` -> `eclipse-temurin:21-jre-alpine`), non-root `farmwise` user, `-XX:MaxRAMPercentage=75.0`, `$PORT` binding.
   - [frontend/Dockerfile](file:///C:/Users/maram/Desktop/FarmWise/frontend/Dockerfile): Node 24 Alpine build -> `nginx:1.27-alpine` runtime.
   - [docker-compose.prod.yml](file:///C:/Users/maram/Desktop/FarmWise/docker-compose.prod.yml): Complete container orchestrator definition with healthchecks, resource limits, and network isolation.

2. **Single Page Application (SPA) Routing & Headers**:
   - [frontend/vercel.json](file:///C:/Users/maram/Desktop/FarmWise/frontend/vercel.json): Rewrites for Vercel edge.
   - [frontend/netlify.toml](file:///C:/Users/maram/Desktop/FarmWise/frontend/netlify.toml): Redirects and header rules for Netlify.
   - [frontend/public/_redirects](file:///C:/Users/maram/Desktop/FarmWise/frontend/public/_redirects): Standard fallback rule for Cloudflare Pages.
   - [frontend/nginx.conf](file:///C:/Users/maram/Desktop/FarmWise/frontend/nginx.conf): Gzip compression, immutable static asset caching, and security headers.

3. **CI/CD Automation**:
   - [.github/workflows/ci.yml](file:///C:/Users/maram/Desktop/FarmWise/.github/workflows/ci.yml): Automated secret scan, Node 22 frontend verification, and Java 21 Maven test suite.
   - [.github/workflows/deploy-prepare.yml](file:///C:/Users/maram/Desktop/FarmWise/.github/workflows/deploy-prepare.yml): Credential-gated Docker container packaging.

---

## 4. Required User Credentials to Finalize Live Launch

In accordance with strict safety rules, **no paid cloud infrastructure was provisioned or billed without explicit user approval and credentials**.

To finalize the live public launch on the web, provide the following 3 account credentials or connect them in your cloud consoles:

### 1. PostgreSQL Database Instance (Render, Neon, or Supabase)
- **Required**:
  - `SPRING_DATASOURCE_URL` (e.g. `jdbc:postgresql://<host>:5432/farmwise?sslmode=require`)
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`

### 2. Render Web Service Deployment (Backend)
- In [dashboard.render.com](https://dashboard.render.com):
  - Click **New +** → **Web Service** → Connect `https://github.com/marammanoj840/FarmWise`.
  - Root directory: `backend-spring`.
  - Environment: `Docker`.
  - Inject environment variables from [PRODUCTION_ENVIRONMENT.md](file:///C:/Users/maram/Desktop/FarmWise/PRODUCTION_ENVIRONMENT.md).

### 3. Cloudflare Pages Deployment (Frontend)
- In Cloudflare Dashboard:
  - Click **Workers & Pages** → **Create application** → **Pages** → Connect `https://github.com/marammanoj840/FarmWise`.
  - Root directory: `frontend`.
  - Build command: `npm run build`.
  - Build output directory: `dist`.
  - Environment variable: `VITE_API_BASE_URL=https://api.yourdomain.com/api/v1` (or your Render backend URL).
