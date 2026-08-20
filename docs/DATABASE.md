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

### `platform_settings`

Global or tenant-scoped configuration (non-secret values only). Unique on generated `tenant_scope` + `setting_key`.

## Phase 2 identity schema

Flyway `V2__identity_rbac_sessions.sql` adds:

- `permissions`, `roles`, `role_permissions`
- `users`, `user_roles`
- `auth_sessions` (hashed refresh tokens, rotation family)
- `password_reset_tokens`, `email_verification_tokens`
- `audit_logs` (append-only)

System roles and permission grants are seeded. No user passwords are stored in Flyway. The first platform admin is created at boot when `AUDIT_PLATFORM_BOOTSTRAP_ADMIN_EMAIL` and `AUDIT_PLATFORM_BOOTSTRAP_ADMIN_PASSWORD` are set and the user table is empty.

`users.tenant_id` is null for platform super admins.

## Isolation rules

- Tenant-owned tables **must** include `tenant_id` and an index on it.
- Queries **must** filter by tenant from the authenticated principal.
- Do not create cross-tenant foreign keys except to `tenants.id`.

## Local databases

Compose creates `audit_platform`. Tests that use Testcontainers create an ephemeral MySQL 8. Unit/web tests do not require a database.
