# Database

Engine: **MySQL 8** with Flyway migrations in `backend/src/main/resources/db/migration`.

Naming: `snake_case` tables and columns. InnoDB, `utf8mb4`.

## Phase 1 schema

### `tenants`

SaaS tenant (certification body / paying organization).

| Column | Type | Notes |
| --- | --- | --- |
| `id` | CHAR(36) | PK, UUID |
| `code` | VARCHAR(64) | Unique business code |
| `name` | VARCHAR(255) | Legal/display name |
| `status` | VARCHAR(32) | `ACTIVE`, `SUSPENDED`, `PROVISIONING` |
| `created_at` | DATETIME(6) | |
| `updated_at` | DATETIME(6) | |
| `created_by` | VARCHAR(64) | |
| `updated_by` | VARCHAR(64) | |
| `deleted_at` | DATETIME(6) | Soft delete |
| `version` | INT | Optimistic lock |

Indexes: unique `code` where `deleted_at` is null is enforced with unique `code` for Phase 1 (codes are not reused).

### `platform_settings`

Global or tenant-scoped configuration (feature flags, mail, storage keys **names**, never secret values).

| Column | Type | Notes |
| --- | --- | --- |
| `id` | CHAR(36) | PK |
| `tenant_id` | CHAR(36) | Null = platform-global |
| `setting_key` | VARCHAR(128) | |
| `setting_value` | TEXT | Non-secret values only |
| `created_at` / `updated_at` | DATETIME(6) | |
| `created_by` / `updated_by` | VARCHAR(64) | |
| `version` | INT | |

Unique (`tenant_id`, `setting_key`).

## Planned (not in Phase 1)

See the master entity list in the product brief: users, roles, clients, audits, findings, certificates, documents, invoices, etc. Each will be introduced with its own versioned Flyway script.

## Isolation rules

- Tenant-owned tables **must** include `tenant_id` and an index on it.
- Queries **must** filter by tenant from `TenantContext` / principal, never from an unverified client-supplied id alone after Phase 2.
- Do not create cross-tenant foreign keys except to `tenants.id`.

## Local databases

Compose creates `audit_platform`. Tests that use Testcontainers create an ephemeral MySQL 8. Unit/web tests do not require a database.
