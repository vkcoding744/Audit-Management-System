# Testing

## Backend

Stack: JUnit 5, Mockito, Spring MockMvc, Testcontainers MySQL 8.

Phase 1 coverage:

- Public system health/info
- Unauthenticated `/api/v1/**` (non-public) returns 401
- Exception envelope shape
- Correlation ID filter
- Tenant context isolation of thread-local state
- Flyway + JPA against Testcontainers MySQL when Docker is present

## Frontend

Vitest + Testing Library for the shell and health client.

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
