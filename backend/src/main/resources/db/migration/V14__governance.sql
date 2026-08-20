CREATE TABLE complaints (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    complaint_number VARCHAR(32) NOT NULL,
    client_id CHAR(36) NULL,
    subject VARCHAR(255) NOT NULL,
    source VARCHAR(32) NOT NULL,
    received_on DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    description TEXT NULL,
    resolution TEXT NULL,
    closed_on DATE NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_complaints_tenant_number (tenant_id, complaint_number),
    KEY idx_complaints_tenant_client_status (tenant_id, client_id, status),
    CONSTRAINT fk_complaints_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_complaints_client FOREIGN KEY (client_id) REFERENCES clients (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE appeals (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    appeal_number VARCHAR(32) NOT NULL,
    client_id CHAR(36) NULL,
    certificate_id CHAR(36) NULL,
    finding_id CHAR(36) NULL,
    subject VARCHAR(255) NOT NULL,
    received_on DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    outcome VARCHAR(32) NULL,
    description TEXT NULL,
    decision_notes TEXT NULL,
    decided_on DATE NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_appeals_tenant_number (tenant_id, appeal_number),
    KEY idx_appeals_tenant_client_status (tenant_id, client_id, status),
    CONSTRAINT fk_appeals_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_appeals_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_appeals_certificate FOREIGN KEY (certificate_id) REFERENCES certificates (id),
    CONSTRAINT fk_appeals_finding FOREIGN KEY (finding_id) REFERENCES findings (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE risks (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    risk_number VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(32) NOT NULL,
    likelihood INT NULL,
    impact INT NULL,
    status VARCHAR(32) NOT NULL,
    description TEXT NULL,
    mitigation TEXT NULL,
    closed_on DATE NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_risks_tenant_number (tenant_id, risk_number),
    KEY idx_risks_tenant_status (tenant_id, status),
    CONSTRAINT fk_risks_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE impartiality_records (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    impartiality_number VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    auditor_id CHAR(36) NULL,
    client_id CHAR(36) NULL,
    identified_on DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    description TEXT NULL,
    review_notes TEXT NULL,
    closed_on DATE NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_impartiality_tenant_number (tenant_id, impartiality_number),
    KEY idx_impartiality_tenant_status (tenant_id, status),
    CONSTRAINT fk_impartiality_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_impartiality_auditor FOREIGN KEY (auditor_id) REFERENCES auditors (id),
    CONSTRAINT fk_impartiality_client FOREIGN KEY (client_id) REFERENCES clients (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'COMPLAINT_UPDATE', 'Update complaints', 'governance', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'APPEAL_UPDATE', 'Update appeals', 'governance', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'RISK_UPDATE', 'Update risks and impartiality', 'governance', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN (
    'COMPLAINT_VIEW', 'COMPLAINT_UPDATE',
    'APPEAL_VIEW', 'APPEAL_UPDATE',
    'RISK_VIEW', 'RISK_UPDATE'
)
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'CERTIFICATION_MANAGER',
    'CERTIFICATION_DECISION_MAKER',
    'TECHNICAL_REVIEWER',
    'AUDIT_MANAGER'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN ('COMPLAINT_VIEW', 'APPEAL_VIEW')
WHERE r.code IN ('CLIENT_ADMIN', 'CLIENT_USER')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
