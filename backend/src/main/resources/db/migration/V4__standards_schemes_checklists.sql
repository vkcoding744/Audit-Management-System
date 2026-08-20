CREATE TABLE standards (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    publisher VARCHAR(255) NULL,
    edition VARCHAR(64) NULL,
    description TEXT NULL,
    status VARCHAR(32) NOT NULL,
    published_at DATETIME(6) NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_standards_tenant_code (tenant_id, code),
    KEY idx_standards_tenant_status (tenant_id, status),
    CONSTRAINT fk_standards_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE standard_clauses (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    standard_id CHAR(36) NOT NULL,
    parent_id CHAR(36) NULL,
    clause_code VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    requirement_text TEXT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_clauses_standard_code (standard_id, clause_code),
    KEY idx_clauses_tenant_standard (tenant_id, standard_id),
    KEY idx_clauses_parent (parent_id),
    CONSTRAINT fk_clauses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_clauses_standard FOREIGN KEY (standard_id) REFERENCES standards (id),
    CONSTRAINT fk_clauses_parent FOREIGN KEY (parent_id) REFERENCES standard_clauses (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE schemes (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    accreditation_body VARCHAR(255) NULL,
    cycle_months INT NULL,
    surveillance_interval_months INT NULL,
    status VARCHAR(32) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_schemes_tenant_code (tenant_id, code),
    KEY idx_schemes_tenant_status (tenant_id, status),
    CONSTRAINT fk_schemes_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE scheme_standards (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    scheme_id CHAR(36) NOT NULL,
    standard_id CHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_scheme_standards (scheme_id, standard_id),
    KEY idx_scheme_standards_tenant (tenant_id, scheme_id),
    CONSTRAINT fk_scheme_standards_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_scheme_standards_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id),
    CONSTRAINT fk_scheme_standards_standard FOREIGN KEY (standard_id) REFERENCES standards (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE checklists (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    scheme_id CHAR(36) NOT NULL,
    standard_id CHAR(36) NULL,
    name VARCHAR(255) NOT NULL,
    version_label VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_checklists_scheme_version (scheme_id, name, version_label),
    KEY idx_checklists_tenant_scheme (tenant_id, scheme_id),
    CONSTRAINT fk_checklists_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_checklists_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id),
    CONSTRAINT fk_checklists_standard FOREIGN KEY (standard_id) REFERENCES standards (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE checklist_items (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    checklist_id CHAR(36) NOT NULL,
    clause_id CHAR(36) NULL,
    title VARCHAR(500) NOT NULL,
    guidance TEXT NULL,
    item_type VARCHAR(32) NOT NULL,
    required TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_checklist_items_checklist (tenant_id, checklist_id),
    KEY idx_checklist_items_clause (clause_id),
    CONSTRAINT fk_checklist_items_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_checklist_items_checklist FOREIGN KEY (checklist_id) REFERENCES checklists (id),
    CONSTRAINT fk_checklist_items_clause FOREIGN KEY (clause_id) REFERENCES standard_clauses (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'STANDARD_CREATE', 'Create standards', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'STANDARD_VIEW', 'View standards', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'STANDARD_UPDATE', 'Update standards', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'STANDARD_DELETE', 'Delete standards', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SCHEME_CREATE', 'Create schemes', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SCHEME_VIEW', 'View schemes', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SCHEME_UPDATE', 'Update schemes', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SCHEME_DELETE', 'Delete schemes', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CHECKLIST_CREATE', 'Create checklists', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CHECKLIST_VIEW', 'View checklists', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CHECKLIST_UPDATE', 'Update checklists', 'standards', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CHECKLIST_DELETE', 'Delete checklists', 'standards', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN (
    'STANDARD_CREATE', 'STANDARD_VIEW', 'STANDARD_UPDATE', 'STANDARD_DELETE',
    'SCHEME_CREATE', 'SCHEME_VIEW', 'SCHEME_UPDATE', 'SCHEME_DELETE',
    'CHECKLIST_CREATE', 'CHECKLIST_VIEW', 'CHECKLIST_UPDATE', 'CHECKLIST_DELETE'
)
WHERE r.code IN ('PLATFORM_SUPER_ADMIN', 'TENANT_ADMIN', 'CERTIFICATION_MANAGER')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN (
    'STANDARD_VIEW', 'STANDARD_UPDATE',
    'SCHEME_VIEW', 'SCHEME_UPDATE',
    'CHECKLIST_CREATE', 'CHECKLIST_VIEW', 'CHECKLIST_UPDATE', 'CHECKLIST_DELETE'
)
WHERE r.code = 'AUDIT_MANAGER'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN (
    'STANDARD_VIEW', 'SCHEME_VIEW', 'CHECKLIST_VIEW', 'CHECKLIST_UPDATE'
)
WHERE r.code IN ('LEAD_AUDITOR', 'TECHNICAL_REVIEWER')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN ('STANDARD_VIEW', 'SCHEME_VIEW', 'CHECKLIST_VIEW')
WHERE r.code IN ('AUDITOR', 'DOCUMENT_CONTROLLER', 'CLIENT_ADMIN', 'READ_ONLY')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
