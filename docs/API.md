# API

Base path: `/api/v1`

OpenAPI: `/v3/api-docs` (JSON) and `/swagger-ui.html` when `audit.api.docs-enabled=true` (default in `dev`).

## Envelope

Success:

```json
{
  "success": true,
  "data": { },
  "error": null,
  "meta": {
    "correlationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "timestamp": "2026-08-20T11:00:00Z"
  }
}
```

Error:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "SYS_VALIDATION",
    "message": "Validation failed",
    "details": [
      { "field": "code", "message": "must not be blank" }
    ]
  },
  "meta": {
    "correlationId": "…",
    "timestamp": "…"
  }
}
```

Send and receive `X-Correlation-Id`. If omitted, the server generates one.

Phase 1 tenant hint: `X-Tenant-Id` is honoured **only** for platform super admins after login. Tenant users are bound to the JWT `tid` claim.

## Phase 1 endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/api/v1/system/health` | Public | Application + database connectivity |
| GET | `/api/v1/system/info` | Public | API version and environment name (no secrets) |
| GET | `/actuator/health/liveness` | Public | Process liveness |
| GET | `/actuator/health/readiness` | Public | Readiness including DB |
| GET | `/actuator/info` | Public | Build info when available |

## Phase 2 endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/login` | Public | Issue access + refresh tokens |
| POST | `/api/v1/auth/refresh` | Public | Rotate refresh token |
| POST | `/api/v1/auth/logout` | Authenticated | Revoke current refresh token |
| POST | `/api/v1/auth/logout-all` | Authenticated | Revoke all sessions |
| POST | `/api/v1/auth/forgot-password` | Public | Queue reset (always generic) |
| POST | `/api/v1/auth/reset-password` | Public | Set new password |
| POST | `/api/v1/auth/verify-email` | Public | Confirm email token |
| GET | `/api/v1/auth/me` | Authenticated | Current user |
| GET | `/api/v1/auth/sessions` | Authenticated | Device sessions |
| DELETE | `/api/v1/auth/sessions/{id}` | Authenticated | Revoke a session |
| GET/POST | `/api/v1/users` | `USER_VIEW` / `USER_CREATE` | User directory |
| PATCH | `/api/v1/users/{id}` | `USER_UPDATE` | Update profile/roles |
| POST | `/api/v1/users/{id}/activate` | `USER_DEACTIVATE` | Enable account |
| POST | `/api/v1/users/{id}/deactivate` | `USER_DEACTIVATE` | Disable account |
| GET | `/api/v1/roles` | `ROLE_VIEW` | System roles |
| GET | `/api/v1/permissions` | `PERMISSION_VIEW` | Permission catalog |
| GET/POST | `/api/v1/tenants` | `TENANT_VIEW` / `TENANT_CREATE` | Tenants |

## Phase 3 endpoints

Clients, sites, and contacts are tenant-scoped. Platform super admins must send `X-Tenant-Id` to list or create. Tenant users are bound to the JWT `tid` claim. Client numbers are assigned as `CLIENT-000001` per tenant via a locked sequence.

Dashboard operational counts for audits, findings, CAPA, certificates, and documents come from live rows via `ClientOperationalMetricsPort`. Payments, complaints, and appeals remain zero until those modules persist data. Site and contact counts are live queries.

`DELETE` responses are HTTP 204 with no envelope.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/api/v1/clients` | `CLIENT_VIEW` | Paginated list; `q` searches name/number; `status` filters |
| GET | `/api/v1/clients/{id}` | `CLIENT_VIEW` | Client detail |
| GET | `/api/v1/clients/{id}/dashboard` | `CLIENT_VIEW` | Client plus operational counts |
| POST | `/api/v1/clients` | `CLIENT_CREATE` | Create (201) |
| PATCH | `/api/v1/clients/{id}` | `CLIENT_UPDATE` | Partial update |
| POST | `/api/v1/clients/{id}/activate` | `CLIENT_UPDATE` | Status `ACTIVE` |
| POST | `/api/v1/clients/{id}/suspend` | `CLIENT_UPDATE` | Status `SUSPENDED` |
| DELETE | `/api/v1/clients/{id}` | `CLIENT_DELETE` | Soft-delete client, sites, contacts (204) |
| GET/POST | `/api/v1/clients/{id}/sites` | `SITE_VIEW` / `SITE_CREATE` | List or create sites |
| PATCH | `/api/v1/sites/{id}` | `SITE_UPDATE` | Update site |
| DELETE | `/api/v1/sites/{id}` | `SITE_DELETE` | Soft-delete site (204) |
| GET/POST | `/api/v1/clients/{id}/contacts` | `CONTACT_VIEW` / `CONTACT_CREATE` | List or create contacts |
| PATCH | `/api/v1/contacts/{id}` | `CONTACT_UPDATE` | Update contact |
| DELETE | `/api/v1/contacts/{id}` | `CONTACT_DELETE` | Soft-delete contact (204) |

Create client body (required: `legalName`): `tradingName`, `registrationNumber`, `taxNumber`, `industry`, `employeeCount`, `email`, `phone`, `website`, `addressLine1`, `addressLine2`, `city`, `state`, `postalCode`, `country`, `status` (`PROSPECT` default), `notes`.

Create site body (required: `name`): address fields, `scope`, `employeeCount`, `processes`, `status` (`ACTIVE` default).

Create contact body (required: `firstName`, `lastName`): `designation`, `email`, `phone`, `department`, `siteId`, `primaryContact`. Setting `primaryContact` true clears the flag on other contacts for that client.

## Phase 4 endpoints

Standards, schemes, clauses, and checklists are tenant-scoped. Platform super admins must send `X-Tenant-Id`. Tenants supply their own codes and clause text; nothing copyrighted is seeded.

Standards: `DRAFT` (editable) → `PUBLISHED` → `SUPERSEDED` or `WITHDRAWN`. Clauses can be added only while `DRAFT`.

Schemes: `DRAFT` → `ACTIVE` (from draft or suspended) → `SUSPENDED` / `RETIRED`. Link standards with `POST .../standards`.

Checklists belong to a scheme. Optional `standardId` must already be linked. Items are editable only in `DRAFT`. Activate requires at least one item.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET/POST | `/api/v1/standards` | `STANDARD_VIEW` / `STANDARD_CREATE` | List (paginated, `q`/`status`) or create (201) |
| GET/PATCH | `/api/v1/standards/{id}` | `STANDARD_VIEW` / `STANDARD_UPDATE` | Detail; patch only in `DRAFT` |
| POST | `/api/v1/standards/{id}/publish` | `STANDARD_UPDATE` | `DRAFT` → `PUBLISHED` |
| POST | `/api/v1/standards/{id}/supersede` | `STANDARD_UPDATE` | `PUBLISHED` → `SUPERSEDED` |
| POST | `/api/v1/standards/{id}/withdraw` | `STANDARD_UPDATE` | `PUBLISHED`/`SUPERSEDED` → `WITHDRAWN` |
| DELETE | `/api/v1/standards/{id}` | `STANDARD_DELETE` | Soft-delete if unused by checklists (204) |
| GET/POST | `/api/v1/standards/{id}/clauses` | `STANDARD_VIEW` / `STANDARD_UPDATE` | Clause tree (flat list) |
| PATCH/DELETE | `/api/v1/clauses/{id}` | `STANDARD_UPDATE` | Update or soft-delete tree |
| GET/POST | `/api/v1/schemes` | `SCHEME_VIEW` / `SCHEME_CREATE` | List or create |
| GET/PATCH | `/api/v1/schemes/{id}` | `SCHEME_VIEW` / `SCHEME_UPDATE` | Detail includes linked standards |
| POST | `/api/v1/schemes/{id}/activate` | `SCHEME_UPDATE` | Activate |
| POST | `/api/v1/schemes/{id}/suspend` | `SCHEME_UPDATE` | Suspend |
| POST | `/api/v1/schemes/{id}/retire` | `SCHEME_UPDATE` | Retire |
| POST | `/api/v1/schemes/{id}/standards` | `SCHEME_UPDATE` | Link `{ standardId }` |
| DELETE | `/api/v1/schemes/{id}/standards/{standardId}` | `SCHEME_UPDATE` | Unlink |
| DELETE | `/api/v1/schemes/{id}` | `SCHEME_DELETE` | Soft-delete if no checklists |
| GET/POST | `/api/v1/schemes/{id}/checklists` | `CHECKLIST_VIEW` / `CHECKLIST_CREATE` | List or create |
| GET/PATCH | `/api/v1/checklists/{id}` | `CHECKLIST_VIEW` / `CHECKLIST_UPDATE` | Detail with items |
| POST | `/api/v1/checklists/{id}/activate` | `CHECKLIST_UPDATE` | `DRAFT` → `ACTIVE` |
| POST | `/api/v1/checklists/{id}/archive` | `CHECKLIST_UPDATE` | `ACTIVE` → `ARCHIVED` |
| DELETE | `/api/v1/checklists/{id}` | `CHECKLIST_DELETE` | Soft-delete checklist and items |
| POST | `/api/v1/checklists/{id}/items` | `CHECKLIST_UPDATE` | Add item (201) |
| PATCH/DELETE | `/api/v1/checklist-items/{id}` | `CHECKLIST_UPDATE` | Update or soft-delete item |

## Phase 5 endpoints

Auditor profiles are tenant-scoped. Employee numbers are `AUD-%06d`. Competency must name a standard and/or scheme and has `validFrom`/`validTo`. `GET .../eligibility` is the assignment gate: expired, suspended, missing competency, inactive auditor, or an `UNAVAILABLE` window returns `eligible: false` with reason codes. Phase 6 planning must call this rather than inventing a second rule.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET/POST | `/api/v1/auditors` | `AUDITOR_VIEW` / `AUDITOR_CREATE` | List (paginated, `q`/`status`) or create |
| GET/PATCH | `/api/v1/auditors/{id}` | `AUDITOR_VIEW` / `AUDITOR_UPDATE` | Profile |
| GET | `/api/v1/auditors/{id}/eligibility` | `AUDITOR_VIEW` | Query `standardId` and/or `schemeId`, optional `on` (ISO date) |
| DELETE | `/api/v1/auditors/{id}` | `AUDITOR_DELETE` | Soft-delete (204) |
| GET/POST | `/api/v1/auditors/{id}/qualifications` | `AUDITOR_VIEW` / `AUDITOR_UPDATE` | Qualifications |
| DELETE | `/api/v1/auditors/qualifications/{id}` | `AUDITOR_UPDATE` | Soft-delete qualification |
| GET/POST | `/api/v1/auditors/{id}/competencies` | `AUDITOR_VIEW` / `AUDITOR_UPDATE` | Competencies |
| POST | `/api/v1/competencies/{id}/suspend` | `AUDITOR_UPDATE` | Suspend |
| POST | `/api/v1/competencies/{id}/revoke` | `AUDITOR_UPDATE` | Revoke |
| DELETE | `/api/v1/competencies/{id}` | `AUDITOR_UPDATE` | Soft-delete competency |
| GET/POST | `/api/v1/auditors/{id}/availability` | `AUDITOR_VIEW` / `AUDITOR_UPDATE` | Availability windows |
| DELETE | `/api/v1/availability/{id}` | `AUDITOR_UPDATE` | Soft-delete window |

Eligibility reasons: `AUDITOR_INACTIVE`, `AUDITOR_SUSPENDED`, `NO_COMPETENCY`, `COMPETENCY_EXPIRED`, `COMPETENCY_SUSPENDED`, `UNAVAILABLE`.

## Phase 6 endpoints

Programmes and audits are tenant-scoped. Numbers are `PROG-%06d` and `AUDIT-%06d` via `crm_sequences`. A programme binds a client and scheme (optional standard that must already be linked to the scheme). Audits start as `PLANNED`. `POST .../schedule` requires planned start/end dates and a `LEAD` assignment. Phase 6 does not start or complete fieldwork.

Assignment calls `AuditorEligibilityService.evaluate` with the audit standard/scheme and the planned start date (or today). Ineligible auditors fail with `SYS_VALIDATION` and the eligibility reason codes. At most one lead per audit.

Client dashboard `upcomingAudits` counts `PLANNED`, `SCHEDULED`, and `IN_PROGRESS`. `completedAudits` counts `COMPLETED`.

`DELETE` responses are HTTP 204 with no envelope.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET/POST | `/api/v1/programmes` | `AUDIT_VIEW` / `AUDIT_CREATE` | Paginated list (`clientId`/`status`) or create (201) |
| GET/PATCH | `/api/v1/programmes/{id}` | `AUDIT_VIEW` / `AUDIT_UPDATE` | Detail; patch unless completed/cancelled |
| POST | `/api/v1/programmes/{id}/activate` | `AUDIT_UPDATE` | `DRAFT` → `ACTIVE` |
| POST | `/api/v1/programmes/{id}/complete` | `AUDIT_UPDATE` | `ACTIVE` → `COMPLETED` |
| POST | `/api/v1/programmes/{id}/cancel` | `AUDIT_UPDATE` | Cancel unless already completed |
| DELETE | `/api/v1/programmes/{id}` | `AUDIT_DELETE` | Soft-delete if no audits (204) |
| GET | `/api/v1/programmes/{id}/audits` | `AUDIT_VIEW` | Audits in the programme |
| GET/POST | `/api/v1/audits` | `AUDIT_VIEW` / `AUDIT_CREATE` | Paginated list (`clientId`/`status`) or create (201) |
| GET/PATCH | `/api/v1/audits/{id}` | `AUDIT_VIEW` / `AUDIT_UPDATE` | Detail includes sites and assignments |
| POST | `/api/v1/audits/{id}/schedule` | `AUDIT_UPDATE` | `PLANNED` → `SCHEDULED` |
| POST | `/api/v1/audits/{id}/cancel` | `AUDIT_UPDATE` | Cancel unless completed |
| DELETE | `/api/v1/audits/{id}` | `AUDIT_DELETE` | Soft-delete if `PLANNED` (204) |
| GET/POST | `/api/v1/audits/{id}/sites` | `AUDIT_VIEW` / `AUDIT_UPDATE` | Sites in scope |
| DELETE | `/api/v1/audit-sites/{id}` | `AUDIT_UPDATE` | Remove site from scope (204) |
| GET/POST | `/api/v1/audits/{id}/assignments` | `AUDIT_VIEW` / `AUDIT_ASSIGN` | Team; eligibility enforced on create |
| DELETE | `/api/v1/assignments/{id}` | `AUDIT_ASSIGN` | Unassign (204) |

Create programme body (required: `clientId`, `schemeId`, `name`): `standardId`, `cycleStartOn`, `cycleEndOn`, `notes`. Status starts `DRAFT`.

Create audit body (required: `programmeId`, `name`): `auditType` (`INITIAL` default), `stage` (`NOT_APPLICABLE` default), `checklistId` (must belong to the programme scheme), `plannedStartOn`, `plannedEndOn`, `notes`.

Create assignment body (required: `auditorId`): `assignmentRole` (`TEAM` default; `LEAD` \| `TEAM` \| `TECHNICAL_EXPERT` \| `TRAINEE` \| `OBSERVER`).

## Phase 7 endpoints

Fieldwork starts only from `SCHEDULED`. `POST .../start` copies checklist items onto `audit_checklist_responses` (title/guidance frozen). Draft checklists cannot be started. Completing requires every **required** item to have a result other than `NOT_ASSESSED`. Nonconformity and observation require a comment. Findings are not created in this phase.

Results: `NOT_ASSESSED`, `CONFORMING`, `NONCONFORMING`, `NOT_APPLICABLE`, `OBSERVATION`.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/api/v1/audits/{id}/start` | `AUDIT_UPDATE` | `SCHEDULED` → `IN_PROGRESS`; snapshot checklist |
| POST | `/api/v1/audits/{id}/complete` | `AUDIT_UPDATE` | `IN_PROGRESS` → `COMPLETED` |
| PATCH | `/api/v1/audits/{id}/execution` | `AUDIT_UPDATE` | Opening/closing notes while in progress |
| GET | `/api/v1/audits/{id}/responses` | `AUDIT_VIEW` | Frozen checklist responses |
| GET | `/api/v1/audit-responses/{id}` | `AUDIT_VIEW` | One response |
| PATCH | `/api/v1/audit-responses/{id}` | `AUDIT_UPDATE` | Record result while in progress |

Patch response body (required: `result`): `comment`.

## Phase 8 endpoints

Findings belong to an audit and inherit its client. Raise only when the audit is `IN_PROGRESS` or `COMPLETED`. Closed findings cannot be patched. Major and minor findings require at least one CAPA, and no open CAPA, before `POST .../close`. Observations and OFIs can close without CAPA. Overdue CAPA is an open action whose `dueOn` is before today.

Numbers: `FIND-%06d`, `CAPA-%06d`. Severity: `MAJOR`, `MINOR`, `OBSERVATION`, `OFI`. Finding status: `OPEN`, `CLOSED`. CAPA status: `OPEN`, `COMPLETED`, `CANCELLED`.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET/POST | `/api/v1/findings` | `AUDIT_VIEW` / `FINDING_CREATE` | Paginated list (`clientId`/`status`) or create (201) |
| GET/PATCH | `/api/v1/findings/{id}` | `AUDIT_VIEW` / `FINDING_UPDATE` | Detail includes CAPA; patch only while `OPEN` |
| POST | `/api/v1/findings/{id}/close` | `FINDING_CLOSE` | `OPEN` → `CLOSED` |
| GET | `/api/v1/audits/{id}/findings` | `AUDIT_VIEW` | Findings for one audit |
| GET/POST | `/api/v1/findings/{id}/capa` | `AUDIT_VIEW` / `FINDING_UPDATE` | List or add CAPA (201) |
| PATCH | `/api/v1/capa/{id}` | `FINDING_UPDATE` | Update open CAPA |
| POST | `/api/v1/capa/{id}/complete` | `FINDING_UPDATE` | `OPEN` → `COMPLETED` |

Create finding body (required: `auditId`, `title`, `description`): `severity` (`MINOR` default), `siteId`, `responseId`, `clauseId`, `notes`.

Create CAPA body (required: `description`, `dueOn`): `notes`.

## Phase 9 endpoints

Certificates are numbered `CERT-%06d` per tenant. Create a draft from a **completed** audit; client, scheme, standard, and programme are copied from that audit. Issue requires no **OPEN** `MAJOR` or `MINOR` findings on the source audit (observations and OFIs may remain open) and at most one `ACTIVE` certificate per `(tenant, client, scheme)`.

Status: `DRAFT` → issue `ACTIVE` → suspend `SUSPENDED` → reinstate `ACTIVE` → withdraw `WITHDRAWN`. `expired` is `true` when status is `ACTIVE` and `expiresOn` is before today (UTC). Decisions (`ISSUE`, `SUSPEND`, `REINSTATE`, `WITHDRAW`) are append-only. Surveillance: `PLANNED`, `COMPLETED`, `CANCELLED`; planning is not allowed on `WITHDRAWN`.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET/POST | `/api/v1/certificates` | `CERTIFICATE_VIEW` / `CERTIFICATE_ISSUE` | Paginated list (`clientId`/`status`) or create draft (201) |
| GET | `/api/v1/certificates/{id}` | `CERTIFICATE_VIEW` | Detail includes decisions and surveillance |
| POST | `/api/v1/certificates/{id}/issue` | `CERTIFICATE_ISSUE` | `DRAFT` → `ACTIVE` |
| POST | `/api/v1/certificates/{id}/suspend` | `CERTIFICATE_SUSPEND` | `ACTIVE` → `SUSPENDED`; body `{ reason }` |
| POST | `/api/v1/certificates/{id}/reinstate` | `CERTIFICATE_ISSUE` | `SUSPENDED` → `ACTIVE`; body `{ reason }` |
| POST | `/api/v1/certificates/{id}/withdraw` | `CERTIFICATE_WITHDRAW` | `ACTIVE` or `SUSPENDED` → `WITHDRAWN`; body `{ reason }` |
| POST | `/api/v1/certificates/{id}/surveillance` | `CERTIFICATE_ISSUE` | Plan a visit (201) |
| POST | `/api/v1/surveillance/{id}/complete` | `CERTIFICATE_ISSUE` | `PLANNED` → `COMPLETED` |

Create body (required: `auditId`, `expiresOn`): `validFrom` (defaults to today UTC), `scopeText`, `nextSurveillanceOn`, `notes`. `expiresOn` cannot be before `validFrom`.

## Phase 10 endpoints

Documents are numbered `DOC-%06d` per tenant. Upload is `multipart/form-data` with a required `file` part. Bytes are stored through `ObjectStoragePort` (local filesystem or S3). Download returns the raw file (not the JSON envelope) and requires `DOCUMENT_DOWNLOAD`. Delete is HTTP 204 and soft-deletes metadata after removing the blob.

Link types: `GENERAL`, `CLIENT`, `AUDIT`, `FINDING`, `CERTIFICATE`. Non-`GENERAL` uploads require `linkedId` of a record in the same tenant; `clientId` is copied from that record. Categories: `EVIDENCE`, `CONTROLLED`, `REPORT`, `OTHER`. Allowed types include PDF, common images, Office/OpenDocument, CSV, plain text, and zip. Executables are rejected.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/api/v1/documents` | `DOCUMENT_VIEW` | Paginated list (`clientId` / `linkedType` / `linkedId`) |
| POST | `/api/v1/documents` | `DOCUMENT_UPLOAD` | Upload (201); form fields: `title`, `clientId`, `linkedType`, `linkedId`, `category`, `notes` |
| GET | `/api/v1/documents/{id}` | `DOCUMENT_VIEW` | Metadata |
| GET | `/api/v1/documents/{id}/content` | `DOCUMENT_DOWNLOAD` | File bytes (`Content-Disposition: attachment`) |
| DELETE | `/api/v1/documents/{id}` | `DOCUMENT_DELETE` | Soft-delete metadata and remove blob (204) |

## Status codes

| Code | Use |
| --- | --- |
| 200 | Success |
| 201 | Created (later phases) |
| 400 | Validation / bad request |
| 401 | Unauthenticated |
| 403 | Authenticated but not permitted |
| 404 | Not found |
| 409 | Conflict / optimistic lock |
| 429 | Rate limited (when enabled) |
| 500 | Unexpected server error |

## Pagination (later list APIs)

Query: `page` (0-based), `size` (max 100), `sort`.
Response `data` will use `PageResponse`.
