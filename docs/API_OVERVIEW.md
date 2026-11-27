# FarmWise Versioned REST API (`/api/v1`)

All protected endpoints require `Authorization: Bearer <accessToken>`. Errors return structured JSON (`timestamp`, `status`, `error`, `code`, `message`, `path`, `fieldErrors`).

| Domain | Endpoints | Auth / Role |
|---|---|---|
| **Health** | `GET /api/v1/health`, `GET /actuator/health` | Public |
| **Public** | `GET /api/v1/public/overview`, `POST /api/v1/public/contact` | Public |
| **Auth** | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `POST /auth/forgot-password`, `POST /auth/reset-password`, `POST /auth/verify-email`, `GET /auth/me` | Public (except `/me`, `/resend-verification`) |
| **Users** | `GET/PUT /users/me/profile`, `PUT /users/me/password`, `GET/DELETE /users/me/sessions` | Authenticated |
| **Farmer Dashboard** | `GET /farmer/dashboard` | Authenticated |
| **Farms** | `GET/POST /farms`, `GET/PUT/DELETE /farms/{id}` | Owner / Admin |
| **Crops** | `GET /crops/catalog`, `GET/POST /crops`, `GET/PUT/DELETE /crops/{id}` | Catalog Public; Plantings Owner |
| **Cultivation** | `GET/POST/DELETE /cultivation/activities`, `GET/POST /cultivation/tasks`, `PATCH /cultivation/tasks/{id}/status` | Owner / Admin |
| **Inventory** | `GET/POST /inventory`, `PUT/DELETE /inventory/{id}`, `GET/POST /inventory/{id}/transactions` | Owner / Admin |
| **Finance & Reports** | `GET/POST/DELETE /finance/expenses`, `GET/POST/DELETE /finance/income`, `GET /finance/summary`, `GET /finance/reports/export?type=...&format=CSV\|PDF` | Owner / Admin |
| **Weather** | `GET /weather/farm/{farmId}`, `GET /weather/lookup`, `GET /weather/farm/{farmId}/history` | Farm routes Owner; `/lookup` Public |
| **Soil & Crop Intelligence** | `GET/POST/DELETE /soil/records`, `GET/POST /soil/observations`, `GET /soil/knowledge`, `GET /soil/knowledge/suitability` | Knowledge Public; Records Owner |
| **AI Assistant** | `GET /assistant/status`, `GET /assistant/conversations`, `GET /assistant/conversations/{id}/messages`, `POST /assistant/ask` | Authenticated (Owner context) |
| **Marketplace & Orders** | `GET /marketplace/categories`, `GET /products`, `POST/PUT /products`, `GET/POST/PATCH/DELETE /marketplace/cart/**`, `GET/POST /orders`, `GET /marketplace/supplier/**` | Catalog Public; Cart/Orders Auth; Supplier Routes `SUPPLIER` |
| **Produce** | `GET /produce`, `GET /produce/{id}`, `GET /marketplace/farmer/produce`, `POST/PUT/DELETE /produce`, `POST/GET /produce/{id}/inquiries`, `PATCH /produce/inquiries/{id}/reply` | Browse Public; Listings `FARMER`; Inquiries Auth |
| **Market Prices** | `GET /market-prices` | Public |
| **Experts & Consultations** | `GET /experts`, `GET/PUT /experts/me/profile`, `POST /consultations`, `GET /consultations/mine`, `GET /consultations/expert/assigned`, `PATCH /consultations/{id}/respond`, `GET/POST /consultations/appointments` | Directory Public; Profile/Respond `EXPERT` |
| **Notifications** | `GET /notifications`, `GET /notifications/unread-count`, `PATCH /notifications/{id}/read`, `PATCH /notifications/read-all`, `GET/PUT /notifications/preferences` | Authenticated |
| **Admin** | `GET /admin/stats`, `GET/PATCH /admin/users/**`, `PATCH /admin/experts/{id}/verify`, `GET/PATCH /admin/suppliers/**`, `GET/PATCH /admin/products/**`, `GET/PATCH /admin/produce/**`, `GET /admin/audit-logs`, `GET/PATCH /admin/contact-inquiries/**`, `POST /admin/notifications`, `GET /admin/system-health` | `ROLE_ADMIN` |
