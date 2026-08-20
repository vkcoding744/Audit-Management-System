CREATE TABLE leads (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    lead_number VARCHAR(32) NOT NULL,
    organisation_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(64) NULL,
    source VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    converted_client_id CHAR(36) NULL,
    converted_at DATETIME(6) NULL,
    lost_reason TEXT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_leads_tenant_number (tenant_id, lead_number),
    KEY idx_leads_tenant_status (tenant_id, status),
    CONSTRAINT fk_leads_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_leads_converted_client FOREIGN KEY (converted_client_id) REFERENCES clients (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'LEAD_UPDATE', 'Update leads', 'crm', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code = 'LEAD_UPDATE'
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'SALES_MANAGER',
    'SALES_EXECUTIVE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
