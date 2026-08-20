# Architecture

## Product

**Audit Platform** is a multi-tenant SaaS for professional audit, inspection, testing, and certification bodies. It is a generic, configurable product. It does not encode any proprietary workflow, branding, or UI from a specific certification company.

## Current phase

**Phase 10** adds document metadata, authenticated upload/download/delete, and an object-storage SPI. The default adapter writes to the local filesystem; `audit.storage.provider=s3` uses S3 (or a path-style endpoint such as MinIO). Files may be linked to a client, audit, finding, or certificate. Executables and unknown types are rejected. Client dashboard document counts are live for rows with a `client_id`.

Phase 1 foundation remains: modular monolith, Flyway, API envelope, CORS/headers, health, tenant discriminator columns.

## Style of architecture

**Modular monolith**, REST, DTO-based APIs, versioned under `/api/v1`.

Bounded contexts live as Java packages under `com.auditplatform`. Package boundaries are the extraction seams for future services.

| Package | Bounded context | Phase |
| --- | --- | --- |
| `common` | API envelope, errors, tenancy, persistence, logging, security filters | 1 |
| `system` | Platform health/info (no business data) | 1 |
| `tenant` | Tenant registry | 1 (schema + entity), 2+ (admin APIs) |
| `identity` | Users, roles, permissions, sessions | 2 |
| `crm` | Clients, sites, contacts, leads | 3, 11 |
| `standards` | Standards, schemes, clauses, checklists | 4 |
| `auditor` | Auditor profiles, competency, availability | 5 |
| `audit` | Programs, planning, execution, findings, CAPA | 6–8 |
| `certification` | Certificates, decisions, surveillance | 9 |
| `document` | Document management + storage SPI | 10 |
| `finance` | Quotes, invoices, payments | 12 |
| `training` | Training records, competency assessments | 13 |
| `governance` | Complaints, appeals, risk, impartiality | 14 |
| `notification` | Templates, channels, jobs | 15 |
| `reporting` | Report builder, exports | 16 |
| `ai` | Provider-agnostic AI SPI | 17 |
| `dashboard` | Aggregated metrics | 18 |

Controllers never contain business rules. Persistence entities are never returned from HTTP APIs.

## Multi-tenancy

**Shared database, shared schema, discriminator column `tenant_id`.**

Rationale for Phase 1:

- Fits a single modular monolith and Flyway.
- Lowest operational cost for early SaaS.
- Isolation is enforced in application services and (later) query filters, not by trusting the client.

Every tenant-owned row includes `tenant_id`. Platform-level rows (the `tenants` table itself, global settings) have no tenant owner.

`TenantContext` is bound from the JWT after authentication:

- Tenant users: tenant id comes from the token. A mismatched `X-Tenant-Id` is **403**.
- Platform super admins: `tenant_id` on the user is null. They may send `X-Tenant-Id` to operate in a tenant. The header is ignored for everyone else.

Cross-tenant access is a defect. Services load by id then `IsolationService.assertSameTenant`.

Cross-tenant access is a defect. Tests in later phases must prove Tenant A cannot read Tenant B.

## Persistence conventions

- Primary keys: UUID stored as `CHAR(36)`.
- Audit columns: `created_at`, `updated_at`, `created_by`, `updated_by`.
- Optimistic locking: `version` (integer).
- Soft delete: `deleted_at` nullable where the domain needs restore/history.
- Hibernate Open Session in View is **disabled**.
- Pagination is mandatory for list APIs.

Base types:

- `AuditableEntity` — timestamps, actors, version
- `TenantAwareEntity` — extends auditable and adds `tenant_id`

## API conventions

Envelope:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "meta": {
    "correlationId": "uuid",
    "timestamp": "ISO-8601"
  }
}
```

Errors use the same envelope with `success: false` and `error.code` from a stable enum (`ErrorCode`).

Public routes:

- `GET /api/v1/system/health`
- `GET /api/v1/system/info`
- Auth login/refresh/forgot/reset/verify
- Actuator liveness/readiness
- OpenAPI UI (non-production by default)

All other `/api/v1/**` routes require authentication and permission checks.

## Security baseline (Phase 1)

- Stateless sessions (`SessionCreationPolicy.STATELESS`)
- CSRF disabled for the Bearer-token API (documented; cookie session login is not used)
- CORS origins from configuration, not `*`
- Security headers (CSP for API is limited; nosniff; deny frames)
- Default Spring user **disabled**
- Secrets only from environment variables
- Rate limiting filter exists as an in-memory strategy, **off by default** (Redis-backed limiter in a later phase)

Authentication is JWT access tokens plus rotating opaque refresh tokens (Phase 2). MFA columns exist; TOTP is not enforced yet.

## Frontend

React 18 + Vite + TypeScript + Tailwind. The UI calls live APIs for health, identity, clients, standards, schemes, checklists, auditors, programmes, audits, fieldwork, findings, CAPA, certificates, decisions, surveillance, and documents. Client dashboard upcoming/completed audit counts, open findings, overdue CAPA, active certificates, certificates expiring within 90 days, and document counts come from persisted rows. Finance still reports zero. It does not mock certification data or copyrighted clause text.

## Infrastructure

Docker Compose runs MySQL 8, backend, and frontend (Nginx). Optional profiles: `redis`, `mailhog`.

AWS-ready: 12-factor config, health probes, no baked secrets, object storage SPI (local filesystem or S3).

## Explicit non-goals for Phase 10

- PDF certificate templates and public verification portals
- Full QMS controlled-document revision/approval workflows
- Antivirus/malware scanning and OCR
- Public unauthenticated file URLs
- Finance, complaints, appeals
- Bundled ISO/IEC clause libraries
- AI providers, Elasticsearch
