# API

Base path: `/api/v1`

OpenAPI: `/v3/api-docs` (JSON) and `/swagger-ui.html` when `audit.api.docs-enabled=true` (default in `dev`).

## Envelope

Success:

```json
{
  "success": true,
  "data": { },
  "error": null,
  "meta": {
    "correlationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "timestamp": "2026-08-20T11:00:00Z"
  }
}
```

Error:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "SYS_VALIDATION",
    "message": "Validation failed",
    "details": [
      { "field": "code", "message": "must not be blank" }
    ]
  },
  "meta": {
    "correlationId": "…",
    "timestamp": "…"
  }
}
```

Send and receive `X-Correlation-Id`. If omitted, the server generates one.

Phase 1 tenant hint: `X-Tenant-Id` is honoured **only** for platform super admins after login. Tenant users are bound to the JWT `tid` claim.

## Phase 1 endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/api/v1/system/health` | Public | Application + database connectivity |
| GET | `/api/v1/system/info` | Public | API version and environment name (no secrets) |
| GET | `/actuator/health/liveness` | Public | Process liveness |
| GET | `/actuator/health/readiness` | Public | Readiness including DB |
| GET | `/actuator/info` | Public | Build info when available |

## Phase 2 endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/login` | Public | Issue access + refresh tokens |
| POST | `/api/v1/auth/refresh` | Public | Rotate refresh token |
| POST | `/api/v1/auth/logout` | Authenticated | Revoke current refresh token |
| POST | `/api/v1/auth/logout-all` | Authenticated | Revoke all sessions |
| POST | `/api/v1/auth/forgot-password` | Public | Queue reset (always generic) |
| POST | `/api/v1/auth/reset-password` | Public | Set new password |
| POST | `/api/v1/auth/verify-email` | Public | Confirm email token |
| GET | `/api/v1/auth/me` | Authenticated | Current user |
| GET | `/api/v1/auth/sessions` | Authenticated | Device sessions |
| DELETE | `/api/v1/auth/sessions/{id}` | Authenticated | Revoke a session |
| GET/POST | `/api/v1/users` | `USER_VIEW` / `USER_CREATE` | User directory |
| PATCH | `/api/v1/users/{id}` | `USER_UPDATE` | Update profile/roles |
| POST | `/api/v1/users/{id}/activate` | `USER_DEACTIVATE` | Enable account |
| POST | `/api/v1/users/{id}/deactivate` | `USER_DEACTIVATE` | Disable account |
| GET | `/api/v1/roles` | `ROLE_VIEW` | System roles |
| GET | `/api/v1/permissions` | `PERMISSION_VIEW` | Permission catalog |
| GET/POST | `/api/v1/tenants` | `TENANT_VIEW` / `TENANT_CREATE` | Tenants |

## Phase 3 endpoints

Clients, sites, and contacts are tenant-scoped. Platform super admins must send `X-Tenant-Id` to list or create. Tenant users are bound to the JWT `tid` claim. Client numbers are assigned as `CLIENT-000001` per tenant via a locked sequence.

Dashboard operational counts (audits, findings, CAPA, certificates, payments, documents, complaints, appeals) come from `ClientOperationalMetricsPort`. The default adapter returns zeros until those modules persist data. Site and contact counts are live queries.

`DELETE` responses are HTTP 204 with no envelope.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/api/v1/clients` | `CLIENT_VIEW` | Paginated list; `q` searches name/number; `status` filters |
| GET | `/api/v1/clients/{id}` | `CLIENT_VIEW` | Client detail |
| GET | `/api/v1/clients/{id}/dashboard` | `CLIENT_VIEW` | Client plus operational counts |
| POST | `/api/v1/clients` | `CLIENT_CREATE` | Create (201) |
| PATCH | `/api/v1/clients/{id}` | `CLIENT_UPDATE` | Partial update |
| POST | `/api/v1/clients/{id}/activate` | `CLIENT_UPDATE` | Status `ACTIVE` |
| POST | `/api/v1/clients/{id}/suspend` | `CLIENT_UPDATE` | Status `SUSPENDED` |
| DELETE | `/api/v1/clients/{id}` | `CLIENT_DELETE` | Soft-delete client, sites, contacts (204) |
| GET/POST | `/api/v1/clients/{id}/sites` | `SITE_VIEW` / `SITE_CREATE` | List or create sites |
| PATCH | `/api/v1/sites/{id}` | `SITE_UPDATE` | Update site |
| DELETE | `/api/v1/sites/{id}` | `SITE_DELETE` | Soft-delete site (204) |
| GET/POST | `/api/v1/clients/{id}/contacts` | `CONTACT_VIEW` / `CONTACT_CREATE` | List or create contacts |
| PATCH | `/api/v1/contacts/{id}` | `CONTACT_UPDATE` | Update contact |
| DELETE | `/api/v1/contacts/{id}` | `CONTACT_DELETE` | Soft-delete contact (204) |

Create client body (required: `legalName`): `tradingName`, `registrationNumber`, `taxNumber`, `industry`, `employeeCount`, `email`, `phone`, `website`, `addressLine1`, `addressLine2`, `city`, `state`, `postalCode`, `country`, `status` (`PROSPECT` default), `notes`.

Create site body (required: `name`): address fields, `scope`, `employeeCount`, `processes`, `status` (`ACTIVE` default).

Create contact body (required: `firstName`, `lastName`): `designation`, `email`, `phone`, `department`, `siteId`, `primaryContact`. Setting `primaryContact` true clears the flag on other contacts for that client.

## Phase 4 endpoints

Standards, schemes, clauses, and checklists are tenant-scoped. Platform super admins must send `X-Tenant-Id`. Tenants supply their own codes and clause text; nothing copyrighted is seeded.

Standards: `DRAFT` (editable) → `PUBLISHED` → `SUPERSEDED` or `WITHDRAWN`. Clauses can be added only while `DRAFT`.

Schemes: `DRAFT` → `ACTIVE` (from draft or suspended) → `SUSPENDED` / `RETIRED`. Link standards with `POST .../standards`.

Checklists belong to a scheme. Optional `standardId` must already be linked. Items are editable only in `DRAFT`. Activate requires at least one item.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET/POST | `/api/v1/standards` | `STANDARD_VIEW` / `STANDARD_CREATE` | List (paginated, `q`/`status`) or create (201) |
| GET/PATCH | `/api/v1/standards/{id}` | `STANDARD_VIEW` / `STANDARD_UPDATE` | Detail; patch only in `DRAFT` |
| POST | `/api/v1/standards/{id}/publish` | `STANDARD_UPDATE` | `DRAFT` → `PUBLISHED` |
| POST | `/api/v1/standards/{id}/supersede` | `STANDARD_UPDATE` | `PUBLISHED` → `SUPERSEDED` |
| POST | `/api/v1/standards/{id}/withdraw` | `STANDARD_UPDATE` | `PUBLISHED`/`SUPERSEDED` → `WITHDRAWN` |
| DELETE | `/api/v1/standards/{id}` | `STANDARD_DELETE` | Soft-delete if unused by checklists (204) |
| GET/POST | `/api/v1/standards/{id}/clauses` | `STANDARD_VIEW` / `STANDARD_UPDATE` | Clause tree (flat list) |
| PATCH/DELETE | `/api/v1/clauses/{id}` | `STANDARD_UPDATE` | Update or soft-delete tree |
| GET/POST | `/api/v1/schemes` | `SCHEME_VIEW` / `SCHEME_CREATE` | List or create |
| GET/PATCH | `/api/v1/schemes/{id}` | `SCHEME_VIEW` / `SCHEME_UPDATE` | Detail includes linked standards |
| POST | `/api/v1/schemes/{id}/activate` | `SCHEME_UPDATE` | Activate |
| POST | `/api/v1/schemes/{id}/suspend` | `SCHEME_UPDATE` | Suspend |
| POST | `/api/v1/schemes/{id}/retire` | `SCHEME_UPDATE` | Retire |
| POST | `/api/v1/schemes/{id}/standards` | `SCHEME_UPDATE` | Link `{ standardId }` |
| DELETE | `/api/v1/schemes/{id}/standards/{standardId}` | `SCHEME_UPDATE` | Unlink |
| DELETE | `/api/v1/schemes/{id}` | `SCHEME_DELETE` | Soft-delete if no checklists |
| GET/POST | `/api/v1/schemes/{id}/checklists` | `CHECKLIST_VIEW` / `CHECKLIST_CREATE` | List or create |
| GET/PATCH | `/api/v1/checklists/{id}` | `CHECKLIST_VIEW` / `CHECKLIST_UPDATE` | Detail with items |
| POST | `/api/v1/checklists/{id}/activate` | `CHECKLIST_UPDATE` | `DRAFT` → `ACTIVE` |
| POST | `/api/v1/checklists/{id}/archive` | `CHECKLIST_UPDATE` | `ACTIVE` → `ARCHIVED` |
| DELETE | `/api/v1/checklists/{id}` | `CHECKLIST_DELETE` | Soft-delete checklist and items |
| POST | `/api/v1/checklists/{id}/items` | `CHECKLIST_UPDATE` | Add item (201) |
| PATCH/DELETE | `/api/v1/checklist-items/{id}` | `CHECKLIST_UPDATE` | Update or soft-delete item |

## Status codes

| Code | Use |
| --- | --- |
| 200 | Success |
| 201 | Created (later phases) |
| 400 | Validation / bad request |
| 401 | Unauthenticated |
| 403 | Authenticated but not permitted |
| 404 | Not found |
| 409 | Conflict / optimistic lock |
| 429 | Rate limited (when enabled) |
| 500 | Unexpected server error |

## Pagination (later list APIs)

Query: `page` (0-based), `size` (max 100), `sort`.
Response `data` will use `PageResponse`.
