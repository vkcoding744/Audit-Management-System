CREATE TABLE training_records (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    training_number VARCHAR(32) NOT NULL,
    auditor_id CHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    provider VARCHAR(255) NULL,
    planned_on DATE NULL,
    completed_on DATE NULL,
    hours INT NULL,
    expires_on DATE NULL,
    standard_id CHAR(36) NULL,
    scheme_id CHAR(36) NULL,
    status VARCHAR(32) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_training_records_tenant_number (tenant_id, training_number),
    KEY idx_training_records_tenant_auditor_status (tenant_id, auditor_id, status),
    CONSTRAINT fk_training_records_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_training_records_auditor FOREIGN KEY (auditor_id) REFERENCES auditors (id),
    CONSTRAINT fk_training_records_standard FOREIGN KEY (standard_id) REFERENCES standards (id),
    CONSTRAINT fk_training_records_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE competency_assessments (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    assessment_number VARCHAR(32) NOT NULL,
    auditor_id CHAR(36) NOT NULL,
    competency_id CHAR(36) NULL,
    standard_id CHAR(36) NULL,
    scheme_id CHAR(36) NULL,
    assessed_on DATE NOT NULL,
    assessor_name VARCHAR(255) NULL,
    result VARCHAR(32) NULL,
    status VARCHAR(32) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_competency_assessments_tenant_number (tenant_id, assessment_number),
    KEY idx_competency_assessments_tenant_auditor_status (tenant_id, auditor_id, status),
    CONSTRAINT fk_competency_assessments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_competency_assessments_auditor FOREIGN KEY (auditor_id) REFERENCES auditors (id),
    CONSTRAINT fk_competency_assessments_competency FOREIGN KEY (competency_id) REFERENCES auditor_competencies (id),
    CONSTRAINT fk_competency_assessments_standard FOREIGN KEY (standard_id) REFERENCES standards (id),
    CONSTRAINT fk_competency_assessments_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'TRAINING_UPDATE', 'Update training and assessments', 'training', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code = 'TRAINING_UPDATE'
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'HR_COMPETENCY_MANAGER',
    'AUDIT_MANAGER'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
