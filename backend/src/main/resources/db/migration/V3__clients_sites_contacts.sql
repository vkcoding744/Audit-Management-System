CREATE TABLE clients (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    client_number VARCHAR(32) NOT NULL,
    legal_name VARCHAR(255) NOT NULL,
    trading_name VARCHAR(255) NULL,
    registration_number VARCHAR(64) NULL,
    tax_number VARCHAR(64) NULL,
    industry VARCHAR(128) NULL,
    employee_count INT NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(64) NULL,
    website VARCHAR(255) NULL,
    address_line1 VARCHAR(255) NULL,
    address_line2 VARCHAR(255) NULL,
    city VARCHAR(128) NULL,
    state VARCHAR(128) NULL,
    postal_code VARCHAR(32) NULL,
    country VARCHAR(128) NULL,
    status VARCHAR(32) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_clients_tenant_number (tenant_id, client_number),
    KEY idx_clients_tenant_status (tenant_id, status),
    KEY idx_clients_tenant_name (tenant_id, legal_name),
    CONSTRAINT fk_clients_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sites (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    client_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address_line1 VARCHAR(255) NULL,
    address_line2 VARCHAR(255) NULL,
    city VARCHAR(128) NULL,
    state VARCHAR(128) NULL,
    postal_code VARCHAR(32) NULL,
    country VARCHAR(128) NULL,
    scope TEXT NULL,
    employee_count INT NULL,
    processes TEXT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_sites_tenant_client (tenant_id, client_id),
    CONSTRAINT fk_sites_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sites_client FOREIGN KEY (client_id) REFERENCES clients (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE contacts (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    client_id CHAR(36) NOT NULL,
    site_id CHAR(36) NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    designation VARCHAR(128) NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(64) NULL,
    department VARCHAR(128) NULL,
    primary_contact TINYINT(1) NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_contacts_tenant_client (tenant_id, client_id),
    KEY idx_contacts_site (site_id),
    CONSTRAINT fk_contacts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_contacts_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_contacts_site FOREIGN KEY (site_id) REFERENCES sites (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE crm_sequences (
    tenant_id CHAR(36) NOT NULL,
    sequence_name VARCHAR(64) NOT NULL,
    next_value BIGINT NOT NULL,
    PRIMARY KEY (tenant_id, sequence_name),
    CONSTRAINT fk_crm_sequences_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'CLIENT_DELETE', 'Delete clients', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SITE_CREATE', 'Create sites', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SITE_VIEW', 'View sites', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SITE_UPDATE', 'Update sites', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SITE_DELETE', 'Delete sites', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CONTACT_CREATE', 'Create contacts', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CONTACT_VIEW', 'View contacts', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CONTACT_UPDATE', 'Update contacts', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CONTACT_DELETE', 'Delete contacts', 'crm', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN (
    'CLIENT_DELETE', 'SITE_CREATE', 'SITE_VIEW', 'SITE_UPDATE', 'SITE_DELETE',
    'CONTACT_CREATE', 'CONTACT_VIEW', 'CONTACT_UPDATE', 'CONTACT_DELETE'
)
WHERE r.code IN ('PLATFORM_SUPER_ADMIN', 'TENANT_ADMIN')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN (
    'CLIENT_DELETE', 'SITE_CREATE', 'SITE_VIEW', 'SITE_UPDATE', 'SITE_DELETE',
    'CONTACT_CREATE', 'CONTACT_VIEW', 'CONTACT_UPDATE', 'CONTACT_DELETE'
)
WHERE r.code = 'SALES_MANAGER'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN ('SITE_CREATE', 'SITE_VIEW', 'CONTACT_CREATE', 'CONTACT_VIEW', 'CONTACT_UPDATE')
WHERE r.code = 'SALES_EXECUTIVE'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN ('SITE_VIEW', 'CONTACT_VIEW')
WHERE r.code IN (
    'CERTIFICATION_MANAGER', 'AUDIT_MANAGER', 'LEAD_AUDITOR', 'DOCUMENT_CONTROLLER',
    'CLIENT_ADMIN', 'READ_ONLY'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
