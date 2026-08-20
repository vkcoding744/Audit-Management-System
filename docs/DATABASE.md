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

## Phase 3 CRM schema

Flyway `V3__clients_sites_contacts.sql` adds:

### `clients`

Tenant-owned organisation records. Unique `(tenant_id, client_number)`. Status: `PROSPECT`, `ACTIVE`, `SUSPENDED`, `INACTIVE`. Soft delete via `deleted_at`.

### `sites`

Locations belonging to a client. Status: `ACTIVE`, `INACTIVE`. Indexed `(tenant_id, client_id)`.

### `contacts`

People belonging to a client, optional `site_id`. `primary_contact` is application-enforced (one primary per client).

### `crm_sequences`

Per-tenant named counters (`CLIENT`) used to allocate `CLIENT-%06d` numbers under a pessimistic lock.

V3 also inserts site/contact/client-delete permissions and grants them to admin, sales, and viewer roles as appropriate. Existing `PLATFORM_SUPER_ADMIN` rows receive the new codes via `role_permissions` inserts.

## Phase 4 standards schema

Flyway `V4__standards_schemes_checklists.sql` adds:

### `standards`

Tenant-owned normative documents. Unique `(tenant_id, code)`. Status: `DRAFT`, `PUBLISHED`, `SUPERSEDED`, `WITHDRAWN`.

### `standard_clauses`

Hierarchical clauses (`parent_id`). Unique `(standard_id, clause_code)`.

### `schemes`

Certification/inspection programmes. Unique `(tenant_id, code)`. Status: `DRAFT`, `ACTIVE`, `SUSPENDED`, `RETIRED`. Optional cycle and surveillance interval in months.

### `scheme_standards`

Many-to-many link between a scheme and standards.

### `checklists` / `checklist_items`

Versioned checklists (`version_label`) under a scheme, optional `standard_id`. Item types: `QUESTION`, `EVIDENCE`, `GUIDANCE`. Unique `(scheme_id, name, version_label)`.

No ISO/IEC (or other) clause libraries are inserted. Tenants enter their own text.

## Phase 5 auditor schema

Flyway `V5__auditor_profiles_competency.sql` adds:

### `auditors`

Staff/contractor profiles. Unique `(tenant_id, employee_number)`. Optional `user_id` link to a login. Status: `ACTIVE`, `INACTIVE`, `SUSPENDED`. Employment: `EMPLOYEE`, `CONTRACTOR`.

### `auditor_qualifications`

Named credentials (title, issuer, dates) not tied to a standard.

### `auditor_competencies`

Standard and/or scheme competence with `competency_role` (`LEAD`, `TEAM`, `TECHNICAL_EXPERT`, `TRAINEE`), `valid_from`/`valid_to`, and status `ACTIVE`/`SUSPENDED`/`REVOKED`. A row with `valid_to` in the past is treated as expired even if status is `ACTIVE`.

### `auditor_availability`

Inclusive date windows, `AVAILABLE` or `UNAVAILABLE`. An overlapping `UNAVAILABLE` window blocks eligibility.

Employee numbers reuse the named-sequence table (`crm_sequences`, name `AUDITOR`).

## Phase 6 audit planning schema

Flyway `V6__audit_programmes_planning.sql` adds:

### `audit_programmes`

Client + scheme (optional standard). Unique `(tenant_id, programme_number)`. Status: `DRAFT`, `ACTIVE`, `COMPLETED`, `CANCELLED`. Soft delete via `deleted_at`.

### `audits`

Visits under a programme. Unique `(tenant_id, audit_number)`. Type: `INITIAL`, `SURVEILLANCE`, `RECERTIFICATION`, `SPECIAL`, `TRANSFER`. Stage: `NOT_APPLICABLE`, `STAGE_1`, `STAGE_2`. Status: `PLANNED`, `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`. Optional checklist must belong to the programme scheme.

### `audit_sites`

Sites in scope for an audit. Unique `(audit_id, site_id)`. Site must belong to the audit client.

### `audit_assignments`

Team members. Unique `(audit_id, auditor_id)`. Role: `LEAD`, `TEAM`, `TECHNICAL_EXPERT`, `TRAINEE`, `OBSERVER`. At most one lead is enforced in the service.

Programme and audit numbers reuse `crm_sequences` (`PROGRAMME`, `AUDIT`).

## Phase 7 execution schema

Flyway `V7__audit_execution.sql` adds `actual_start_on`, `actual_end_on`, `opening_notes`, and `closing_notes` on `audits`, plus:

### `audit_checklist_responses`

Per-visit copy of checklist items. Unique `(audit_id, checklist_item_id)`. `result` is `NOT_ASSESSED`, `CONFORMING`, `NONCONFORMING`, `NOT_APPLICABLE`, or `OBSERVATION`. Title and guidance are stored so later template edits do not rewrite an in-flight visit.

## Phase 8 findings schema

Flyway `V8__findings_capa.sql` adds:

### `findings`

Raised against an audit. Unique `(tenant_id, finding_number)`. Severity: `MAJOR`, `MINOR`, `OBSERVATION`, `OFI`. Status: `OPEN`, `CLOSED`. Optional `site_id`, `response_id` (checklist snapshot), `clause_id`.

### `capa_actions`

Corrective actions under a finding. Unique `(tenant_id, capa_number)`. Status: `OPEN`, `COMPLETED`, `CANCELLED`. Overdue means `OPEN` and `due_on` before today.

Finding and CAPA numbers reuse `crm_sequences` (`FINDING`, `CAPA`).

## Phase 9 certificates schema

Flyway `V9__certificates_decisions.sql` adds:

### `certificates`

Issued (or drafted) against a completed audit. Unique `(tenant_id, certificate_number)`. Status: `DRAFT`, `ACTIVE`, `SUSPENDED`, `WITHDRAWN`. `valid_from` and `expires_on` are required dates.

### `certification_decisions`

Append-only decision log. `decision_type`: `ISSUE`, `SUSPEND`, `REINSTATE`, `WITHDRAW`.

### `certificate_surveillance`

Planned or completed surveillance visits. Status: `PLANNED`, `COMPLETED`, `CANCELLED`.

Certificate numbers reuse `crm_sequences` (`CERTIFICATE` → `CERT-%06d`).

`CERTIFICATE_VIEW` is granted in V9; `CERTIFICATE_ISSUE`, `CERTIFICATE_SUSPEND`, and `CERTIFICATE_WITHDRAW` already exist in V2.

## Phase 10 documents schema

Flyway `V10__documents.sql` adds:

### `documents`

Tenant-owned file metadata. Unique `(tenant_id, document_number)` and `(tenant_id, storage_key)`. Optional `client_id`. Polymorphic link via `linked_type` + `linked_id` (no FK on `linked_id`). Category: `EVIDENCE`, `CONTROLLED`, `REPORT`, `OTHER`. Soft delete via `deleted_at`. Bytes live in object storage, not in MySQL.

Document numbers reuse `crm_sequences` (`DOCUMENT` → `DOC-%06d`).

`DOCUMENT_VIEW` is granted in V10; `DOCUMENT_UPLOAD`, `DOCUMENT_DOWNLOAD`, and `DOCUMENT_DELETE` already exist in V2.

## Phase 11 leads schema

Flyway `V11__leads.sql` adds:

### `leads`

Tenant-owned sales pipeline records. Unique `(tenant_id, lead_number)`. Status: `OPEN`, `QUALIFIED`, `CONVERTED`, `LOST`. Source: `WEBSITE`, `REFERRAL`, `TENDER`, `EVENT`, `OTHER`. Optional `converted_client_id` after convert.

Lead numbers reuse `crm_sequences` (`LEAD` → `LEAD-%06d`).

`LEAD_UPDATE` is granted in V11; `LEAD_VIEW` and `LEAD_CREATE` already exist in V2.

## Isolation rules

- Tenant-owned tables **must** include `tenant_id` and an index on it.
- Queries **must** filter by tenant from the authenticated principal.
- Do not create cross-tenant foreign keys except to `tenants.id`.

## Local databases

Compose creates `audit_platform`. Tests that use Testcontainers create an ephemeral MySQL 8. Unit/web tests do not require a database.
