CREATE TABLE auditors (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    user_id CHAR(36) NULL,
    employee_number VARCHAR(32) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(64) NULL,
    job_title VARCHAR(128) NULL,
    employment_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    base_location VARCHAR(255) NULL,
    country VARCHAR(128) NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auditors_tenant_number (tenant_id, employee_number),
    UNIQUE KEY uk_auditors_tenant_user (tenant_id, user_id),
    KEY idx_auditors_tenant_status (tenant_id, status),
    CONSTRAINT fk_auditors_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_auditors_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auditor_qualifications (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    auditor_id CHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    issuer VARCHAR(255) NULL,
    issued_on DATE NULL,
    expires_on DATE NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_qualifications_auditor (tenant_id, auditor_id),
    CONSTRAINT fk_qualifications_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_qualifications_auditor FOREIGN KEY (auditor_id) REFERENCES auditors (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auditor_competencies (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    auditor_id CHAR(36) NOT NULL,
    standard_id CHAR(36) NULL,
    scheme_id CHAR(36) NULL,
    competency_role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_competencies_auditor (tenant_id, auditor_id),
    KEY idx_competencies_standard (standard_id),
    KEY idx_competencies_scheme (scheme_id),
    CONSTRAINT fk_competencies_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_competencies_auditor FOREIGN KEY (auditor_id) REFERENCES auditors (id),
    CONSTRAINT fk_competencies_standard FOREIGN KEY (standard_id) REFERENCES standards (id),
    CONSTRAINT fk_competencies_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auditor_availability (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    auditor_id CHAR(36) NOT NULL,
    start_on DATE NOT NULL,
    end_on DATE NOT NULL,
    kind VARCHAR(32) NOT NULL,
    reason VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_availability_auditor_dates (tenant_id, auditor_id, start_on, end_on),
    CONSTRAINT fk_availability_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_availability_auditor FOREIGN KEY (auditor_id) REFERENCES auditors (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'AUDITOR_CREATE', 'Create auditor profiles', 'auditor', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDITOR_DELETE', 'Delete auditor profiles', 'auditor', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN ('AUDITOR_CREATE', 'AUDITOR_DELETE')
WHERE r.code IN ('PLATFORM_SUPER_ADMIN', 'TENANT_ADMIN', 'HR_COMPETENCY_MANAGER', 'AUDIT_MANAGER')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN ('AUDITOR_VIEW')
WHERE r.code IN ('CERTIFICATION_MANAGER', 'LEAD_AUDITOR', 'TECHNICAL_REVIEWER', 'READ_ONLY')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
