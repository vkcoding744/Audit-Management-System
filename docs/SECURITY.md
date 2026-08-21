# Security

## Secrets

- Never commit secrets. Use `.env` (gitignored) from `.env.example`.
- Spring configuration binds `AUDIT_PLATFORM_*` variables.
- Passwords, refresh tokens, reset tokens, and JWT secrets are never logged.
- JWT signing key: `AUDIT_PLATFORM_JWT_SECRET` (min 32 characters). Production refuses the documented placeholder.

## Controls

| Control | Status |
| --- | --- |
| BCrypt password hashing (strength 12) | Phase 2 |
| JWT access + rotating refresh tokens | Phase 2 |
| Permission authorities (`hasAuthority`) | Phase 2 |
| Tenant isolation from principal | Phase 2 |
| Login lockout | Phase 2 |
| Password reset / email verification tokens (hashed) | Phase 2 |
| Session/device list + revoke | Phase 2 |
| MFA columns + TOTP setup/enable/disable and login verification | Phase 20 |
| CORS allowlist / security headers | Phase 1 |
| CSRF | Disabled: Bearer API, no cookie session |
| Default Spring user | Disabled |
| Object storage keys namespaced by tenant; downloads authenticated | Phase 10 |
| Outbound email recipient redacted in logs; SMTP optional | Phase 15 |
| Report export downloads authenticated; files namespaced by tenant | Phase 16 |
| AI drafts require human review; vendor keys stay in the environment | Phase 17 |
| Tenant dashboard counts filtered by authenticated tenant | Phase 18 |
| Audit log list is tenant-scoped; `AUDIT_LOG_VIEW` required | Phase 19 |
| Redis-backed rate limit adapter (`audit.rate-limit.provider=redis`) | Phase 20 |
| Hibernate tenant query filter on `TenantAwareEntity` | Phase 20 |

## Authentication

- Login: `POST /api/v1/auth/login` with email + password. If `mfaEnabled`, a 6-digit TOTP is required (`AUTH_MFA_REQUIRED` then `AUTH_MFA_INVALID` on mismatch).
- MFA: `POST /api/v1/auth/mfa/setup` stores an AES-GCM encrypted secret (`AUDIT_PLATFORM_MFA_ENCRYPT_KEY`, else derived from the JWT secret — set a dedicated key in production). Enable with a valid code; disable with code + password.
- Access token: HMAC-SHA256 JWT, short TTL (default 15 minutes), claims: `sub` (user id), `tid`, `sid`, `plat`, `perms`.
- Refresh token: opaque, stored as SHA-256, rotated on use. Reuse of a revoked token in the same family revokes the family.
- Logout revokes the current session; logout-all revokes every session for the user.
- Passwords: BCrypt. Dummy verify when the email is unknown to reduce timing leakage.

## Authorization

Spring Security method security uses **permission codes**, not role names, for API checks (for example `USER_VIEW`, `TENANT_CREATE`). Roles are bundles of permissions seeded as system roles.

Platform super admin receives every permission. Tenant-scoped APIs still apply isolation for non-platform users. Creating or listing clients requires an effective tenant: tenant users use JWT `tid`; platform admins must send `X-Tenant-Id`. Cross-tenant client access returns `AUTH_TENANT_MISMATCH`.

## Tokens in the browser

Phase 2 stores access and refresh tokens in `sessionStorage` and sends `Authorization: Bearer`. Platform admins also persist a selected tenant id in `sessionStorage` and send it as `X-Tenant-Id`. This is XSS-sensitive; httpOnly cookies are a later hardening option (then CSRF must be re-enabled for those routes).

## Email

`OutboundEmailPort` is the only mail API. The default adapter logs that a message was queued (never the token or password). In `dev`, `audit.auth.expose-dev-tokens=true` may return reset/verification tokens in the JSON body so local setup works without SMTP.
