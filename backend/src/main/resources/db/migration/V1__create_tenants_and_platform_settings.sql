CREATE TABLE tenants (
    id CHAR(36) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenants_code (code),
    KEY idx_tenants_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE platform_settings (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NULL,
    setting_key VARCHAR(128) NOT NULL,
    setting_value TEXT NULL,
    tenant_scope CHAR(36) AS (IFNULL(tenant_id, '00000000-0000-0000-0000-000000000000')) STORED,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_platform_settings_scope_key (tenant_scope, setting_key),
    KEY idx_platform_settings_key (setting_key),
    CONSTRAINT fk_platform_settings_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
