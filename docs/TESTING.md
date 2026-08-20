# Testing

## Backend

Stack: JUnit 5, Mockito, Spring MockMvc, Testcontainers MySQL 8.

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

Vitest + Testing Library for the shell, health client, login form, client directory, standards catalogue, and auditor directory.

## Critical scenarios (later phases)

Documented here so they are not lost:

- Unauthorized user cannot access API
- Tenant A cannot access Tenant B
- Missing permission denied
- Expired auditor competency blocks assignment (Phase 5 eligibility API)
- Closed finding cannot be edited outside workflow
- Certificate issue requires approvals
- Expired certificate identified
- Overdue payment calculated

## Running

See `docs/DEVELOPMENT.md`.
