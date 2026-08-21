CREATE TABLE ai_generations (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    generation_number VARCHAR(32) NOT NULL,
    purpose VARCHAR(64) NOT NULL,
    prompt TEXT NOT NULL,
    output TEXT NOT NULL,
    provider VARCHAR(64) NOT NULL,
    model VARCHAR(128) NOT NULL,
    prompt_version VARCHAR(32) NOT NULL,
    linked_type VARCHAR(32) NULL,
    linked_id CHAR(36) NULL,
    status VARCHAR(32) NOT NULL,
    error_message VARCHAR(512) NULL,
    reviewed_by VARCHAR(64) NULL,
    reviewed_at DATETIME(6) NULL,
    review_notes VARCHAR(512) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_generations_tenant_number (tenant_id, generation_number),
    KEY idx_ai_generations_tenant_status (tenant_id, status),
    CONSTRAINT fk_ai_generations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'AI_VIEW', 'View AI generations', 'ai', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AI_UPDATE', 'Generate and review AI drafts', 'ai', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN ('AI_VIEW', 'AI_UPDATE')
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'CERTIFICATION_MANAGER',
    'AUDIT_MANAGER',
    'TECHNICAL_REVIEWER',
    'LEAD_AUDITOR'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code = 'AI_VIEW'
WHERE r.code = 'READ_ONLY'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
