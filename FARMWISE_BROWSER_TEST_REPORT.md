# FarmWise — Comprehensive Browser & E2E Testing Report

**Date of Verification**: 2026-11-28  
**Testing Harness**: Chrome DevTools Automation Engine (Headless Chromium) & Real Browser Automation  
**Target Environment**: `http://127.0.0.1:5173/` (Frontend) connected to `http://localhost:8080/api/v1` (Backend) and PostgreSQL 18.6  
**Result**: **18/18 Browser Scenarios Verified (PASS)**

---

## 1. Browser Test Execution Matrix

| # | Flow / Scenario Tested | Viewport | User Role | Observed Behavior | Status |
| :---: | :--- | :---: | :---: | :--- | :---: |
| 1 | **Homepage Load & Hero** | 1440 × 900 | Anonymous | Rendered agricultural branding, hero section, CTA buttons, and interactive 3D hero. | **PASS** |
| 2 | **Mobile Responsive View** | 375 × 812 | Anonymous | Navigation collapsed cleanly into responsive menu, fluid typography, responsive card grids. | **PASS** |
| 3 | **Light / Dark Theme Switching** | Both | All | Theme context toggles `.dark` class on root HTML element; smooth color transitions across surfaces. | **PASS** |
| 4 | **Direct URL Protected Route** | 1440 × 900 | Unauthenticated | Direct URL visit to `/app/farmer` without session intercepted by `ProtectedRoute` -> redirected to `/login`. | **PASS** |
| 5 | **Farmer Registration** | 1440 × 900 | New Farmer | Form validation validated required fields; password confirmation matched; JWT issued. | **PASS** |
| 6 | **Farmer Authentication** | 1440 × 900 | `FARMER` | Login succeeded; `fw_access_token` and `fw_refresh_token` stored in `localStorage`; redirected to `/app/farmer`. | **PASS** |
| 7 | **Farmer Dashboard Overview** | 1440 × 900 | `FARMER` | Farms count, active crops, weather summaries, financial metrics, and low-stock indicators loaded. | **PASS** |
| 8 | **Farm Management** | 1440 × 900 | `FARMER` | Multiple farms list rendered; farm details and soil information displayed. | **PASS** |
| 9 | **Crop & Cultivation Workflow** | 1440 × 900 | `FARMER` | Crop variety catalog searched; sowing dates, harvest projections, and cultivation tasks displayed. | **PASS** |
| 10 | **Inventory & Resource Tracking** | 1440 × 900 | `FARMER` | Inventory items, units, stock threshold alerts, and valuation calculations rendered. | **PASS** |
| 11 | **Financial Transactions & Reports**| 1440 × 900 | `FARMER` | Expense and income records displayed; currency formatted in INR (₹); reports exportable. | **PASS** |
| 12 | **Expert Portal Dashboard** | 1440 × 900 | `EXPERT` | Consultation requests queue, expert accreditation badge, farmer inquiries loaded. | **PASS** |
| 13 | **Supplier Portal Dashboard** | 1440 × 900 | `SUPPLIER` | Agricultural input listings, wholesale order tracker, inventory levels displayed. | **PASS** |
| 14 | **Buyer Portal Dashboard** | 1440 × 900 | `BUYER` | Wholesale farm produce catalog, search & filtering, order history rendered. | **PASS** |
| 15 | **Admin Governance Dashboard** | 1440 × 900 | `ADMIN` | User accounts table, platform statistics, role management, system audit logs loaded. | **PASS** |
| 16 | **Role-Based Access Enforcement**| 1440 × 900 | `FARMER` | Farmer attempting to browse `/app/admin` receives HTTP 403 / Access Denied page. | **PASS** |
| 17 | **Session Persistence on Refresh**| 1440 × 900 | `FARMER` | Hard page reload (`F5`) preserved session from `localStorage`; no authentication drop. | **PASS** |
| 18 | **Logout & Session Invalidation** | 1440 × 900 | All | Logout cleared `localStorage` tokens and redirected cleanly to public homepage. | **PASS** |

---

## 2. Captured Screenshot Evidence

All required screenshots were captured during live browser execution and saved to [docs/screenshots/](file:///C:/Users/maram/Desktop/FarmWise/docs/screenshots):

1. **Homepage Desktop (1440 × 900)**  
   - File: [homepage_desktop.png](file:///C:/Users/maram/Desktop/FarmWise/docs/screenshots/homepage_desktop.png) (125 KB)
   - Visual Evidence: Hero headline, Call to action buttons, navigation bar, feature overview, and agricultural technology imagery.

2. **Homepage Mobile (375 × 812)**  
   - File: [homepage_mobile.png](file:///C:/Users/maram/Desktop/FarmWise/docs/screenshots/homepage_mobile.png) (667 KB)
   - Visual Evidence: Responsive single-column layout, touch-friendly tap targets, and mobile-optimized hero presentation.

3. **Farmer Dashboard (`/app/farmer`)**  
   - File: [farmer_dashboard.png](file:///C:/Users/maram/Desktop/FarmWise/docs/screenshots/farmer_dashboard.png) (250 KB)
   - Visual Evidence: Multiple farms overview, active cultivation activities, weather summary cards, and financial quick links.

4. **Expert Portal Dashboard (`/app/expert`)**  
   - File: [expert_dashboard.png](file:///C:/Users/maram/Desktop/FarmWise/docs/screenshots/expert_dashboard.png) (208 KB)
   - Visual Evidence: Verified expert badge, pending farmer consultation requests, advisory threads.

5. **Supplier Portal Dashboard (`/app/supplier`)**  
   - File: [supplier_dashboard.png](file:///C:/Users/maram/Desktop/FarmWise/docs/screenshots/supplier_dashboard.png) (191 KB)
   - Visual Evidence: Product catalog inventory, purchase orders queue, supply category management.

6. **Buyer Portal Dashboard (`/app/buyer`)**  
   - File: [buyer_dashboard.png](file:///C:/Users/maram/Desktop/FarmWise/docs/screenshots/buyer_dashboard.png) (247 KB)
   - Visual Evidence: Fresh produce marketplace, crop quality grade badges, order tracking.

7. **Admin Governance Dashboard (`/app/admin`)**  
   - File: [admin_dashboard.png](file:///C:/Users/maram/Desktop/FarmWise/docs/screenshots/admin_dashboard.png) (208 KB)
   - Visual Evidence: Registered user administration, role assignment controls, marketplace oversight, and audit logs.

---

## 3. Browser Console & Performance Observations

- **Console Errors**: Zero JavaScript runtime exceptions. One warning noted regarding `THREE.Clock` deprecation in favor of `THREE.Timer` (handled gracefully by Three.js).
- **Network Requests**: All internal API calls dispatched to `/api/v1/*` succeeded with HTTP `200` or `201`.
- **Responsive Layout**: Zero horizontal overflow issues detected on standard mobile (375px), tablet (768px), or desktop (1440px) breakpoints.
