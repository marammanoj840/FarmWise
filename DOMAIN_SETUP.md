# FarmWise — Custom Domain and DNS Configuration Guide

This guide describes how to configure your custom domains, DNS records, HTTPS certificates, and CDN routing for FarmWise in production.

---

## 1. Domain Architecture

We recommend allocating your domains as follows:

| Hostname | Target Service | Example URL | Purpose |
| :--- | :--- | :--- | :--- |
| **`farmwise.ag`** (Apex) | Frontend SPA | `https://farmwise.ag` | Public marketing site, farmer portal, web app |
| **`www.farmwise.ag`** | Frontend SPA | `https://www.farmwise.ag` | Canonical CNAME redirect to apex or mirror |
| **`api.farmwise.ag`** | Backend API | `https://api.farmwise.ag` | Spring Boot 4.2 REST API & Actuator endpoints |
| **`media.farmwise.ag`** | Object Storage CDN | `https://media.farmwise.ag` | Cloudflare R2 / S3 public media CDN for farm/crop photos |

---

## 2. Cloudflare DNS Setup (Recommended)

Managing your DNS through Cloudflare provides edge DDoS mitigation, HTTP/3, Brotli/Gzip compression, automatic SSL/TLS certificate management, and WAF protection.

### DNS Records Table

| Type | Name | Target / Content | Proxy Status | TTL |
| :--- | :--- | :--- | :--- | :--- |
| **CNAME** | `@` (apex) | `farmwise.pages.dev` (or Vercel CNAME `cname.vercel-dns.com`) | Proxied (Orange Cloud) | Auto |
| **CNAME** | `www` | `farmwise.ag` (or `farmwise.pages.dev`) | Proxied (Orange Cloud) | Auto |
| **CNAME** | `api` | `farmwise-backend-6do0.onrender.com` (or Railway/AWS domain) | Proxied (Orange Cloud) | Auto |
| **CNAME** | `media` | Custom domain endpoint for Cloudflare R2 bucket | Proxied (Orange Cloud) | Auto |
| **TXT** | `@` | SPF Record: `v=spf1 include:sendgrid.net ~all` | DNS Only | Auto |
| **TXT** | `_dmarc` | DMARC Record: `v=DMARC1; p=quarantine; rua=mailto:dmarc@farmwise.ag` | DNS Only | Auto |

---

## 3. SSL/TLS Certificate Configuration

### Cloudflare SSL/TLS Mode
- Set Cloudflare SSL/TLS encryption mode to **Full (Strict)**.
  - *Do NOT use "Flexible"* — Flexible mode communicates between Cloudflare and your origin server over plain HTTP, exposing JWTs and passwords in transit.
  - "Full (Strict)" ensures an end-to-end encrypted HTTPS connection from the user's browser, through Cloudflare's edge, all the way to the origin server.

### Edge Certificates & HSTS
1. Under **SSL/TLS** → **Edge Certificates**:
   - Turn **Always Use HTTPS** to **ON**.
   - Enable **Automatic HTTPS Rewrites**.
   - Set **Minimum TLS Version** to **TLS 1.2** (or TLS 1.3).
2. Enable **HSTS (HTTP Strict Transport Security)**:
   - Max-Age: `31536000` (1 year)
   - Include subdomains: `true`
   - Preload: `true`

---

## 4. CORS and Cookie Configuration

### Allowed Origins Alignment
The backend Spring Boot service enforces CORS matching via `CORS_ORIGINS`.
Ensure that the origins specified in your environment variables match the exact scheme and hostname configured in DNS:

```env
CORS_ORIGINS=https://farmwise.ag,https://www.farmwise.ag
```

### Cookie Security Flags
When transmitting auth tokens or session identifiers via cookies:
- **`Secure`**: Must be set so cookies are never sent over unencrypted HTTP.
- **`HttpOnly`**: Must be set so client-side JavaScript cannot access the refresh cookie (mitigating XSS).
- **`SameSite=Lax`** or **`SameSite=Strict`**: Must be set to protect against Cross-Site Request Forgery (CSRF).

---

## 5. Verification Commands

Run the following commands from your terminal to verify that your DNS and SSL setup is functioning:

```bash
# Verify DNS resolution
dig +short farmwise.ag
dig +short api.farmwise.ag

# Verify HTTPS Certificate and TLS Version
curl -Iv https://farmwise.ag 2>&1 | grep -iE 'ssl connection|subject:|issuer:'

# Verify Automatic HTTP -> HTTPS Redirection
curl -I http://farmwise.ag | grep -i 'location: https://'
```
