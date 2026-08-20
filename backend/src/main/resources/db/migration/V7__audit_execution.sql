ALTER TABLE audits
    ADD COLUMN actual_start_on DATE NULL AFTER planned_end_on,
    ADD COLUMN actual_end_on DATE NULL AFTER actual_start_on,
    ADD COLUMN opening_notes TEXT NULL AFTER notes,
    ADD COLUMN closing_notes TEXT NULL AFTER opening_notes;

CREATE TABLE audit_checklist_responses (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    audit_id CHAR(36) NOT NULL,
    checklist_item_id CHAR(36) NOT NULL,
    clause_id CHAR(36) NULL,
    title VARCHAR(500) NOT NULL,
    guidance TEXT NULL,
    item_type VARCHAR(32) NOT NULL,
    required TINYINT(1) NOT NULL,
    sort_order INT NOT NULL,
    result VARCHAR(32) NOT NULL,
    comment TEXT NULL,
    assessed_by VARCHAR(36) NULL,
    assessed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_audit_responses_item (audit_id, checklist_item_id),
    KEY idx_audit_responses_tenant (tenant_id, audit_id),
    CONSTRAINT fk_audit_responses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_audit_responses_audit FOREIGN KEY (audit_id) REFERENCES audits (id),
    CONSTRAINT fk_audit_responses_item FOREIGN KEY (checklist_item_id) REFERENCES checklist_items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
