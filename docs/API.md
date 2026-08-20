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
