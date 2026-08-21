# Testing

## Backend

Stack: JUnit 5, Mockito, Spring MockMvc, Testcontainers MySQL 8.

Phase 22 coverage:

- `SEARCH_VIEW` required to search; other authorities are 403; unauthenticated is 401
- Platform admin without tenant scope cannot search (`SYS_VALIDATION`)
- MySQL adapter searches only the requested tenant and escapes `LIKE` wildcards
- Elasticsearch query JSON always includes a `tenantId` filter

Phase 21 coverage:

- Login without CSRF is forbidden when cookie sessions are enabled
- Login with CSRF sets httpOnly cookies and omits tokens from JSON
- `AP-ACCESS` cookie authenticates `GET /auth/me`

Phase 20 coverage:

- RFC 6238 TOTP SHA-1 vectors (`59` → `287082`)
- Login with MFA enabled: missing code is `AUTH_MFA_REQUIRED`; bad code is `AUTH_MFA_INVALID`
- In-memory rate limiter denies after the per-minute cap
- Isolation service no-arg constructor still works without JPA; filter is skipped when the EntityManager is not in a transaction

Phase 19 coverage:

- `AUDIT_LOG_VIEW` required to list audit logs; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's audit log (`AUTH_TENANT_MISMATCH`)

Phase 18 coverage:

- `DASHBOARD_VIEW` required to read the tenant dashboard; other authorities are 403; unauthenticated is 401
- Platform admin without tenant scope cannot load the dashboard (`SYS_VALIDATION`)
- Summary maps authenticated tenant repository counts

Phase 17 coverage:

- `AI_VIEW` required to list generations; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's AI generation (`AUTH_TENANT_MISMATCH`)
- Approving an already `APPROVED` generation is `SYS_VALIDATION`
- Stub adapter output requires human review and states it must not issue a certificate or close a finding

Phase 16 coverage:

- `REPORT_VIEW` required to list reports; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's report definition (`AUTH_TENANT_MISMATCH`)
- Running an `ARCHIVED` definition is `SYS_VALIDATION`
- CSV renderer quotes fields that contain commas

Phase 15 coverage:

- `NOTIFICATION_VIEW` required to list jobs; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's notification job (`AUTH_TENANT_MISMATCH`)
- Sending an already `SENT` job is `SYS_VALIDATION`
- `due` is true for a queued job whose `scheduledFor` is before now

Phase 14 coverage:

- `COMPLAINT_VIEW` required to list complaints; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's complaint (`AUTH_TENANT_MISMATCH`)
- Closing an already `CLOSED` complaint is `SYS_VALIDATION`
- Deciding an already decided appeal is `SYS_VALIDATION`

Phase 13 coverage:

- `TRAINING_VIEW` required to list training records; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's training record (`AUTH_TENANT_MISMATCH`)
- Completing an already `RECORDED` assessment is `SYS_VALIDATION`
- `expired` is true for a `COMPLETED` training record whose `expiresOn` is before today (UTC)

Phase 12 coverage:

- `INVOICE_VIEW` required to list invoices; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's invoice (`AUTH_TENANT_MISMATCH`)
- Payment exceeding amount due is `SYS_VALIDATION`
- `overdue` is true for an issued invoice whose `dueOn` is before today (UTC)

Phase 11 coverage:

- `LEAD_VIEW` required to list leads; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's lead (`AUTH_TENANT_MISMATCH`)
- Convert of an already converted lead is `SYS_CONFLICT`
- Convert of an open lead creates a prospect client and sets `convertedClientId`

Phase 10 coverage:

- `DOCUMENT_VIEW` required to list documents; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's document (`AUTH_TENANT_MISMATCH`)
- Upload of a disallowed content type is `SYS_VALIDATION`
- Local object storage round-trips bytes and rejects `..` keys

Phase 9 coverage:

- `CERTIFICATE_VIEW` required to list certificates; other authorities are 403; unauthenticated is 401
- Tenant A cannot `get` Tenant B's certificate (`AUTH_TENANT_MISMATCH`)
- Issue is `SYS_VALIDATION` while an open major or minor finding exists on the source audit
- `expired` is true for an `ACTIVE` certificate whose `expiresOn` is before today (UTC)

Phase 8 coverage:

- `AUDIT_VIEW` required to list findings; other authorities are 403
- Tenant A cannot `get` Tenant B's finding
- Closed findings cannot be patched (`SYS_VALIDATION`)

Phase 7 coverage:

- `AUDIT_VIEW` required to list fieldwork responses; other authorities are 403
- Tenant A cannot `get` Tenant B's checklist response
- Completing an audit with unanswered required items is `SYS_VALIDATION`

Phase 6 coverage:

- `AUDIT_VIEW` required to list programmes and audits; other authorities are 403
- Tenant A cannot `get` Tenant B's programme or audit
- Assignment with ineligible competency (`COMPETENCY_EXPIRED`) is `SYS_VALIDATION`

Phase 5 coverage:

- `AUDITOR_VIEW` required to list auditors; other authorities are 403
- Tenant A cannot `get` Tenant B's auditor
- Expired competency is not eligible for assignment; current competency is eligible

Phase 4 coverage:

- `STANDARD_VIEW` / `SCHEME_VIEW` required to list; other authorities are 403
- Tenant A cannot `get` Tenant B's standard
- Published standards cannot be patched

Phase 3 coverage:

- `CLIENT_VIEW` required to list clients; other authorities are 403
- `SITE_VIEW` required to list sites
- Tenant A cannot `get` Tenant B's client
- Platform admin `requireTenantScope` needs `X-Tenant-Id`

Phase 2 coverage:

- Login public, `/me` requires auth
- Permission `USER_VIEW` required to list users; other authorities are 403
- Tenant isolation helper rejects cross-tenant access
- JWT round-trip

- Public system health/info
- Unauthenticated `/api/v1/**` (non-public) returns 401
- Exception envelope shape
- Correlation ID filter
- Tenant context isolation of thread-local state
- Flyway + JPA against Testcontainers MySQL when Docker is present

## Frontend

Vitest + Testing Library for the shell, health client, login form, client directory, leads directory, standards catalogue, auditor directory, programme directory, scheduled-audit fieldwork action, findings directory, certificates directory, documents directory, finance directory, training directory, governance directory, notifications directory, reports directory, AI drafts directory, operations dashboard, audit log directory, MFA controls on sessions, cookie helpers, and tenant search.

## Critical scenarios (later phases)

Documented here so they are not lost:

- Unauthorized user cannot access API
- Tenant A cannot access Tenant B
- Missing permission denied
- Expired auditor competency blocks assignment (Phase 5 eligibility API and Phase 6 assign)
- Closed finding cannot be edited outside workflow (Phase 8)
- Certificate issue requires a completed audit and closed major/minor findings (Phase 9)
- Expired certificate identified (Phase 9 `expired` flag)
- Unauthorized user cannot download another tenant's file (Phase 10 isolation + `DOCUMENT_DOWNLOAD`)
- Overdue payment identified (Phase 12 `overdue` flag; payment cannot exceed amount due)
- Expired completed training identified (Phase 13 `expired` flag; recorded assessments cannot be changed)
- Closed complaint cannot be changed; decided appeal cannot be re-decided (Phase 14)
- Sent notification job cannot be sent again; queued job past `scheduledFor` is `due` (Phase 15)
- Archived report cannot be run; CSV fields with commas are quoted (Phase 16)
- Approved AI draft cannot be approved again; stub output requires human review (Phase 17)
- Tenant dashboard requires `DASHBOARD_VIEW` and an effective tenant (Phase 18)
- Tenant A cannot read Tenant B's audit log (Phase 19)
- MFA login requires a valid TOTP when enabled (Phase 20)
- Cookie-session login requires CSRF; tokens stay out of JSON (Phase 21)
- Tenant search requires `SEARCH_VIEW` and an effective tenant (Phase 22)

## Running

See `docs/DEVELOPMENT.md`.
