# Testing

## Backend

Stack: JUnit 5, Mockito, Spring MockMvc, Testcontainers MySQL 8.

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

Vitest + Testing Library for the shell, health client, login form, and client directory/create form.

## Critical scenarios (later phases)

Documented here so they are not lost:

- Unauthorized user cannot access API
- Tenant A cannot access Tenant B
- Missing permission denied
- Expired auditor competency blocks assignment
- Closed finding cannot be edited outside workflow
- Certificate issue requires approvals
- Expired certificate identified
- Overdue payment calculated

## Running

See `docs/DEVELOPMENT.md`.
