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

## Phase 12 finance schema

Flyway `V12__quotes_invoices_payments.sql` adds:

### `quotes` / `quote_lines`

Tenant-owned commercial offers. Unique `(tenant_id, quote_number)`. Status: `DRAFT`, `ISSUED`, `ACCEPTED`, `DECLINED`. Amounts `DECIMAL(15,2)`.

### `invoices` / `invoice_lines`

Bills to a client. Unique `(tenant_id, invoice_number)`. Optional unique `quote_id`. Status: `DRAFT`, `ISSUED`, `PARTIALLY_PAID`, `PAID`, `VOID`. `amount_paid` is the sum of payments.

### `payments`

Receipts against an invoice. Unique `(tenant_id, payment_number)`. Method: `BANK_TRANSFER`, `CARD`, `CHEQUE`, `OTHER`.

Numbers reuse `crm_sequences` (`QUOTE` → `QUOTE-%06d`, `INVOICE` → `INV-%06d`, `PAYMENT` → `PAY-%06d`).

`INVOICE_VIEW` is granted in V12; `INVOICE_CREATE` and `PAYMENT_RECORD` already exist in V2.

## Phase 13 training schema

Flyway `V13__training_assessments.sql` adds:

### `training_records`

Tenant-owned training evidence for an auditor. Unique `(tenant_id, training_number)`. Status: `PLANNED`, `COMPLETED`, `CANCELLED`. Optional `standard_id` / `scheme_id`.

### `competency_assessments`

Tenant-owned assessment records. Unique `(tenant_id, assessment_number)`. Status: `DRAFT`, `RECORDED`. Result: `PASS`, `FAIL` (null until recorded). Optional `competency_id` must reference a competency of the same auditor.

Numbers reuse `crm_sequences` (`TRAINING` → `TRN-%06d`, `ASSESSMENT` → `ASM-%06d`).

`TRAINING_UPDATE` is granted in V13; `TRAINING_VIEW` already exists in V2.

## Phase 14 governance schema

Flyway `V14__governance.sql` adds:

### `complaints`

Tenant-owned complaints. Unique `(tenant_id, complaint_number)`. Status: `OPEN`, `IN_REVIEW`, `CLOSED`. Source: `CLIENT`, `INTERESTED_PARTY`, `INTERNAL`, `REGULATOR`, `OTHER`. Optional `client_id`.

### `appeals`

Tenant-owned appeals against a decision, certificate, or finding. Unique `(tenant_id, appeal_number)`. Status: `OPEN`, `UNDER_REVIEW`, `UPHELD`, `DISMISSED`. Optional `client_id`, `certificate_id`, `finding_id`.

### `risks`

Tenant-owned risk register. Unique `(tenant_id, risk_number)`. Status: `OPEN`, `MITIGATING`, `CLOSED`. Category: `OPERATIONAL`, `IMPARTIALITY`, `FINANCIAL`, `COMPLIANCE`, `OTHER`. Optional likelihood/impact 1–5.

### `impartiality_records`

Tenant-owned impartiality issues. Unique `(tenant_id, impartiality_number)`. Status: `OPEN`, `REVIEWED`, `CLOSED`. Optional `auditor_id`, `client_id`.

Numbers reuse `crm_sequences` (`COMPLAINT` → `CMP-%06d`, `APPEAL` → `APL-%06d`, `RISK` → `RSK-%06d`, `IMPARTIALITY` → `IMP-%06d`).

`COMPLAINT_UPDATE`, `APPEAL_UPDATE`, and `RISK_UPDATE` are granted in V14; the matching `*_VIEW` permissions already exist in V2.

## Phase 15 notification schema

Flyway `V15__notifications.sql` adds:

### `notification_templates`

Tenant-owned message templates. Unique `(tenant_id, code)`. Channel: `EMAIL`, `IN_APP`. Status: `ACTIVE`, `INACTIVE`. Event type: `GENERIC`, `PASSWORD_RESET`, `CERTIFICATE_EXPIRING`, `CAPA_OVERDUE`, `COMPLAINT_OPEN`, `AUDIT_SCHEDULED`.

### `notification_channels`

One row per tenant and channel type. Unique `(tenant_id, channel)`. `enabled` gates send.

### `notification_jobs`

Outbound queue. Unique `(tenant_id, job_number)`. Status: `QUEUED`, `SENT`, `FAILED`, `CANCELLED`. Optional `template_id`.

Job numbers reuse `crm_sequences` (`NOTIFICATION` → `NTF-%06d`).

`NOTIFICATION_VIEW` and `NOTIFICATION_UPDATE` are granted in V15.

## Phase 16 reporting schema

Flyway `V16__reporting.sql` adds:

### `report_definitions`

Tenant-owned report builder rows. Unique `(tenant_id, report_number)`. Dataset: `CLIENTS`, `AUDITS`, `FINDINGS`, `CERTIFICATES`, `INVOICES`, `COMPLAINTS`. Format: `CSV`, `JSON`. Status: `DRAFT`, `ACTIVE`, `ARCHIVED`. Optional `status_filter`.

### `report_exports`

Generated files stored through `ObjectStoragePort`. Unique `(tenant_id, export_number)`. Status: `QUEUED`, `COMPLETED`, `FAILED`, `CANCELLED`. Optional `storage_key`, `row_count`, `byte_size`, `error_message`.

Numbers reuse `crm_sequences` (`REPORT` → `RPT-%06d`, `EXPORT` → `EXP-%06d`).

`REPORT_VIEW` is granted in V16. `REPORT_EXPORT` already exists in V2.

## Phase 17 AI schema

Flyway `V17__ai_generations.sql` adds:

### `ai_generations`

Tenant-owned drafts. Unique `(tenant_id, generation_number)`. Status: `PENDING_REVIEW`, `APPROVED`, `REJECTED`, `FAILED`. Purpose: `GENERIC`, `FINDING_SUMMARY`, `AUDIT_NARRATIVE`, `COMPLAINT_RESPONSE`. Stores provider, model, prompt version, optional linked record, reviewer, and review notes. Does not store vendor API keys.

Numbers reuse `crm_sequences` (`AI` → `AIG-%06d`).

`AI_VIEW` and `AI_UPDATE` are granted in V17.

## Phase 18 dashboard

Flyway `V18__dashboard.sql` adds `DASHBOARD_VIEW`. There is no new fact table; `GET /api/v1/dashboard` aggregates live tenant-scoped counts from existing modules.

## Phase 19 audit log access

No new table. `GET /api/v1/audit-logs` reads Phase 2 `audit_logs` with tenant isolation. `AUDIT_LOG_VIEW` already exists in V2.

## Phase 20 hardening

No new Flyway version. TOTP secrets use existing `users.mfa_secret_encrypted` (AES-GCM). Hibernate filter `tenantIsolation` applies to `TenantAwareEntity` tables only.

## Phase 21 cookie sessions

No new table. `AP-ACCESS` and `AP-REFRESH` are HTTP cookies only.

## Phase 22 search

Flyway `V19__search.sql` adds `SEARCH_VIEW`. There is no search index table; MySQL queries existing tenant-owned rows.

## Phase 23 notification dispatch

No new Flyway version. Due `QUEUED` jobs (`scheduled_for` at or before now) are selected by `NotificationJobRepository.findDueQueued`. The scheduler does not enable the Hibernate tenant filter (no tenant on the worker thread). HTTP dispatch stays on the current tenant.

## Isolation rules

- Tenant-owned tables **must** include `tenant_id` and an index on it.
- Queries **must** filter by tenant from the authenticated principal.
- Do not create cross-tenant foreign keys except to `tenants.id`.

## Local databases

Compose creates `audit_platform`. Tests that use Testcontainers create an ephemeral MySQL 8. Unit/web tests do not require a database.
