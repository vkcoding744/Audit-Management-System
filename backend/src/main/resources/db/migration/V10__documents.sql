CREATE TABLE documents (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    document_number VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    checksum_sha256 CHAR(64) NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    client_id CHAR(36) NULL,
    linked_type VARCHAR(32) NOT NULL,
    linked_id CHAR(36) NULL,
    category VARCHAR(32) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_documents_tenant_number (tenant_id, document_number),
    UNIQUE KEY uk_documents_tenant_storage_key (tenant_id, storage_key),
    KEY idx_documents_tenant_client (tenant_id, client_id),
    KEY idx_documents_tenant_link (tenant_id, linked_type, linked_id),
    CONSTRAINT fk_documents_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_documents_client FOREIGN KEY (client_id) REFERENCES clients (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'DOCUMENT_VIEW', 'View documents', 'document', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code = 'DOCUMENT_VIEW'
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'CERTIFICATION_MANAGER',
    'AUDIT_MANAGER',
    'LEAD_AUDITOR',
    'AUDITOR',
    'TECHNICAL_REVIEWER',
    'CERTIFICATION_DECISION_MAKER',
    'DOCUMENT_CONTROLLER',
    'CLIENT_ADMIN',
    'CLIENT_USER',
    'READ_ONLY'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
