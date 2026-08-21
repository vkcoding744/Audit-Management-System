CREATE TABLE report_definitions (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    report_number VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(512) NULL,
    dataset VARCHAR(32) NOT NULL,
    format VARCHAR(16) NOT NULL,
    status_filter VARCHAR(64) NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_report_definitions_tenant_number (tenant_id, report_number),
    KEY idx_report_definitions_tenant_status (tenant_id, status),
    KEY idx_report_definitions_tenant_dataset (tenant_id, dataset),
    CONSTRAINT fk_report_definitions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE report_exports (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    definition_id CHAR(36) NOT NULL,
    export_number VARCHAR(32) NOT NULL,
    format VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,
    storage_key VARCHAR(512) NULL,
    content_type VARCHAR(128) NULL,
    row_count INT NULL,
    byte_size BIGINT NULL,
    error_message VARCHAR(512) NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_report_exports_tenant_number (tenant_id, export_number),
    KEY idx_report_exports_tenant_status (tenant_id, status),
    KEY idx_report_exports_definition (definition_id),
    CONSTRAINT fk_report_exports_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_report_exports_definition FOREIGN KEY (definition_id) REFERENCES report_definitions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'REPORT_VIEW', 'View report definitions and exports', 'reporting', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN ('REPORT_VIEW', 'REPORT_EXPORT')
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'CERTIFICATION_MANAGER',
    'AUDIT_MANAGER',
    'TECHNICAL_REVIEWER',
    'ACCOUNTANT',
    'SALES_MANAGER'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code = 'REPORT_VIEW'
WHERE r.code = 'READ_ONLY'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
