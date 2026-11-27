# FarmWise — Cloud Deployment Guide

This guide details recommended cloud hosting architectures, verified pricing, and step-by-step instructions for deploying FarmWise to production.

---

## 1. Hosting Architecture Options & Verified Costs

### Architecture Option A: Unified Modern PaaS (Recommended for Speed & Simplicity)
- **Frontend**: Cloudflare Pages or Vercel
- **Backend API**: Render Web Service (Docker / Native Java)
- **Database**: Render Managed PostgreSQL
- **Object Storage**: Cloudflare R2

| Component | Provider / Tier | Specifications | Verified Cost (USD) |
| :--- | :--- | :--- | :--- |
| **Frontend** | Cloudflare Pages | Unlimited bandwidth, global edge CDN, SSL | **$0 / month** (Free tier) |
| **Backend** | Render Web Service (Starter/Standard) | 0.5–1 CPU, 512MB–2GB RAM, auto-SSL, health checks | **$7 – $25 / month** |
| **Database** | Render Managed PostgreSQL (Basic/Pro) | Shared to 1 Dedicated CPU, 1GB–2GB RAM, 10GB storage, daily backups | **$19 – $45 / month** |
| **Object Storage** | Cloudflare R2 | 10 GB storage free, **$0 egress fees**, $0.015/GB-mo storage | **$0 – $2 / month** |
| **Domain & DNS** | Cloudflare DNS | DDoS protection, automated SSL, HTTP/3, WebSockets | **$0 / month** |
| **Total Estimated Operating Cost** | | | **~$26 – $72 / month** |

---

### Architecture Option B: Serverless & Autoscaling (Recommended for Variable Traffic)
- **Frontend**: Vercel Pro ($20/mo) or Free Hobby ($0/mo)
- **Backend API**: Railway or Google Cloud Run (Containerized)
- **Database**: Neon Serverless PostgreSQL (Scale-to-zero or Launch plan at $0.106/CU-hour + $0.35/GB-month)
- **Object Storage**: Cloudflare R2 or AWS S3 ($0.023/GB-month)
- **Total Estimated Operating Cost**: **~$25 – $65 / month**

---

### Architecture Option C: Enterprise High-Availability (AWS)
- **Frontend**: AWS S3 + CloudFront CDN + Route 53
- **Backend API**: AWS ECS Fargate (2 tasks across 2 Availability Zones behind an Application Load Balancer)
- **Database**: AWS RDS PostgreSQL Multi-AZ (db.t4g.medium)
- **Object Storage**: AWS S3 (Bucket with S3 Versioning & Lifecycle Rules)
- **Total Estimated Operating Cost**: **~$140 – $220 / month**

---

## 2. Step-by-Step Deployment Instructions (Render + Cloudflare Pages)

### Phase 1: Database Provisioning (Render PostgreSQL)
1. Log into your Render dashboard at [dashboard.render.com](https://dashboard.render.com).
2. Click **New +** → **PostgreSQL**.
3. Configure the database:
   - **Name**: `farmwise-postgres-prod`
   - **Database**: `farmwise`
   - **User**: `farmwise`
   - **Region**: Choose closest to target users (e.g. `Singapore` or `Frankfurt` for APAC/India, or `Ohio/Oregon` for Americas).
   - **Plan**: Select **Basic (1GB RAM)** ($19/mo) or higher.
4. Once provisioned, copy the **Internal Connection String** (if backend is on Render) or **External Connection String**.
   Ensure `sslmode=require` is present in the JDBC connection URL:
   ```
   jdbc:postgresql://<host>.render.com:5432/farmwise?sslmode=require
   ```

### Phase 2: Object Storage Setup (Cloudflare R2)
1. In Cloudflare Dashboard, navigate to **R2** → **Create Bucket**.
2. Name the bucket `farmwise-production-media`.
3. Under **Settings** → **Bucket Details**, connect a custom domain (e.g., `media.farmwise.ag`).
4. Under **R2 API Tokens**, click **Create API Token** with Object Read & Write permissions.
5. Record:
   - Access Key ID
   - Secret Access Key
   - Account Endpoint URL (`https://<account_id>.r2.cloudflarestorage.com`)

### Phase 3: Backend Deployment (Render Web Service)
1. In Render Dashboard, click **New +** → **Web Service**.
2. Connect your Git repository (`FarmWise`).
3. Set configuration:
   - **Root Directory**: `backend-spring`
   - **Environment**: **Docker** (uses `backend-spring/Dockerfile`)
   - **Region**: Same region as your Render PostgreSQL database!
   - **Instance Type**: **Starter** ($7/mo) or **Standard** ($25/mo).
4. Set Environment Variables under the **Environment** tab:
   ```env
   SPRING_PROFILES_ACTIVE=prod
   PORT=8080
   SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/farmwise?sslmode=require
   SPRING_DATASOURCE_USERNAME=farmwise
   SPRING_DATASOURCE_PASSWORD=<secure-db-password>
   JWT_SECRET=<64-char-hex-secret>
   CORS_ORIGINS=https://farmwise.ag,https://www.farmwise.ag
   STORAGE_PROVIDER=s3
   S3_BUCKET=farmwise-production-media
   S3_REGION=auto
   S3_ENDPOINT=https://<account_id>.r2.cloudflarestorage.com
   S3_ACCESS_KEY=<r2-access-key>
   S3_SECRET_KEY=<r2-secret-key>
   S3_PUBLIC_BASE_URL=https://media.farmwise.ag
   ```
5. Click **Deploy Web Service**.
6. Render will automatically run the multi-stage Docker build, execute Flyway migrations (`V1` through `V8`), and bind to the public URL `https://api.farmwise.ag` (or `https://farmwise-backend.onrender.com`).
7. Verify backend health by visiting:
   ```bash
   curl -i https://api.farmwise.ag/api/v1/health
   ```
   Expected response: `HTTP 200` with `{"status":"UP","service":"farmwise-backend","databaseStatus":"UP"}`.

### Phase 4: Frontend Deployment (Cloudflare Pages or Vercel)
#### Using Cloudflare Pages:
1. Navigate to Cloudflare Dashboard → **Workers & Pages** → **Create Application** → **Pages** → **Connect to Git**.
2. Build Settings:
   - **Framework preset**: `Vite`
   - **Root directory**: `frontend`
   - **Build command**: `npm run build`
   - **Build output directory**: `dist`
3. Environment Variables:
   - `VITE_API_BASE_URL`: `https://api.farmwise.ag/api/v1`
4. Deploy! Cloudflare Pages uses `frontend/public/_redirects` to handle SPA routing automatically.

---

## 3. Post-Deployment Smoke Test Checklist

Execute this verification script against your deployed endpoints:

```bash
# 1. Verify Backend Health
curl -s https://api.farmwise.ag/api/v1/health | jq .

# 2. Verify Security Headers
curl -s -I https://api.farmwise.ag/api/v1/health | grep -iE 'x-content-type-options|x-frame-options|strict-transport-security'

# 3. Verify Rate Limiting Filter
for i in {1..25}; do curl -s -o /dev/null -w "%{http_code}\n" -X POST https://api.farmwise.ag/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"test@farmwise.ag","password":"wrong"}'; done
# Expected: First 20 return 401/400; remaining return 429 Too Many Requests.

# 4. Verify Frontend SPA Fallback
curl -s -I https://farmwise.ag/farmer/farms | grep -i 'HTTP/2 200'

# 5. Verify Storage Status Endpoint
curl -s https://api.farmwise.ag/api/v1/storage/status | jq .
```
