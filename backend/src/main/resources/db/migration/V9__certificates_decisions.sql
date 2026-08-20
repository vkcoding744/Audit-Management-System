CREATE TABLE certificates (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    certificate_number VARCHAR(32) NOT NULL,
    client_id CHAR(36) NOT NULL,
    scheme_id CHAR(36) NOT NULL,
    standard_id CHAR(36) NULL,
    programme_id CHAR(36) NULL,
    audit_id CHAR(36) NOT NULL,
    scope_text TEXT NULL,
    status VARCHAR(32) NOT NULL,
    valid_from DATE NOT NULL,
    expires_on DATE NOT NULL,
    next_surveillance_on DATE NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_certificates_tenant_number (tenant_id, certificate_number),
    KEY idx_certificates_tenant_client_status (tenant_id, client_id, status),
    KEY idx_certificates_tenant_expiry (tenant_id, status, expires_on),
    CONSTRAINT fk_certificates_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_certificates_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_certificates_scheme FOREIGN KEY (scheme_id) REFERENCES schemes (id),
    CONSTRAINT fk_certificates_standard FOREIGN KEY (standard_id) REFERENCES standards (id),
    CONSTRAINT fk_certificates_programme FOREIGN KEY (programme_id) REFERENCES audit_programmes (id),
    CONSTRAINT fk_certificates_audit FOREIGN KEY (audit_id) REFERENCES audits (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE certification_decisions (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    certificate_id CHAR(36) NOT NULL,
    decision_type VARCHAR(32) NOT NULL,
    reason TEXT NULL,
    decided_on DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_cert_decisions_certificate (certificate_id),
    CONSTRAINT fk_cert_decisions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_cert_decisions_certificate FOREIGN KEY (certificate_id) REFERENCES certificates (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE certificate_surveillance (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    certificate_id CHAR(36) NOT NULL,
    planned_on DATE NOT NULL,
    completed_on DATE NULL,
    status VARCHAR(32) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_cert_surv_certificate (certificate_id),
    CONSTRAINT fk_cert_surv_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_cert_surv_certificate FOREIGN KEY (certificate_id) REFERENCES certificates (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'CERTIFICATE_VIEW', 'View certificates', 'certification', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code = 'CERTIFICATE_VIEW'
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'CERTIFICATION_MANAGER',
    'CERTIFICATION_DECISION_MAKER',
    'AUDIT_MANAGER',
    'TECHNICAL_REVIEWER',
    'CLIENT_ADMIN',
    'CLIENT_USER',
    'READ_ONLY'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
