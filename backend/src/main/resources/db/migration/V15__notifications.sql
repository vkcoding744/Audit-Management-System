CREATE TABLE notification_templates (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_templates_tenant_code (tenant_id, code),
    KEY idx_notification_templates_tenant_status (tenant_id, status),
    CONSTRAINT fk_notification_templates_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification_channels (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    enabled TINYINT(1) NOT NULL,
    from_address VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_channels_tenant_channel (tenant_id, channel),
    CONSTRAINT fk_notification_channels_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification_jobs (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    job_number VARCHAR(32) NOT NULL,
    template_id CHAR(36) NULL,
    channel VARCHAR(32) NOT NULL,
    to_address VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    scheduled_for DATETIME(6) NULL,
    sent_at DATETIME(6) NULL,
    error_message VARCHAR(512) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_jobs_tenant_number (tenant_id, job_number),
    KEY idx_notification_jobs_tenant_status (tenant_id, status),
    CONSTRAINT fk_notification_jobs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_notification_jobs_template FOREIGN KEY (template_id) REFERENCES notification_templates (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'NOTIFICATION_VIEW', 'View notification templates and jobs', 'notification', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'NOTIFICATION_UPDATE', 'Update notification templates, channels, and jobs', 'notification', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN ('NOTIFICATION_VIEW', 'NOTIFICATION_UPDATE')
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'CERTIFICATION_MANAGER',
    'AUDIT_MANAGER'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code = 'NOTIFICATION_VIEW'
WHERE r.code = 'READ_ONLY'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
