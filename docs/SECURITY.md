# Security

## Secrets

- Never commit secrets. Use `.env` (gitignored) from `.env.example`.
- Spring configuration binds `AUDIT_PLATFORM_*` and standard `SPRING_DATASOURCE_*` variables.
- Passwords are never logged. JWT material is not present in Phase 1.

## Phase 1 controls

| Control | Status |
| --- | --- |
| BCrypt password hashing | Phase 2 (dependency not used yet) |
| JWT access/refresh | Phase 2 |
| RBAC / permission checks | Phase 2 |
| Tenant discriminator columns | Phase 1 schema + context |
| CORS allowlist | Phase 1 |
| Security HTTP headers | Phase 1 |
| CSRF | Disabled: API is stateless Bearer (no cookie auth) |
| SQL injection | JPA parameterized queries only |
| Default Spring user | Disabled |
| Rate limiting | In-memory filter, disabled by default |
| Actuator | Only health/info exposed; no heapdump/env |

## CSRF

Browser cookie sessions are not used. The SPA will send `Authorization: Bearer` in Phase 2. CSRF is disabled for the API. If a cookie-based admin session is added later, CSRF must be re-enabled for those routes.

## Headers

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- Referrer-Policy: `no-referrer`
- Permissions-Policy: camera/microphone/geolocation disabled at HTTP layer for API responses

## File uploads

Not implemented in Phase 1. Future work: MIME sniffing, size limits, allowlist, storage path never returned, malware scan SPI.

## Audit log

Immutable audit log table is Phase 2+ (identity events). Correlation IDs are on every request in Phase 1 to support that trail.
