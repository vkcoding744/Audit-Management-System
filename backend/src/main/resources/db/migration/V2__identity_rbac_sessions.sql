CREATE TABLE permissions (
    id CHAR(36) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    module VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_permissions_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512) NULL,
    system_role TINYINT(1) NOT NULL DEFAULT 0,
    role_scope CHAR(36) AS (IFNULL(tenant_id, '00000000-0000-0000-0000-000000000000')) STORED,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_scope_code (role_scope, code),
    KEY idx_roles_tenant (tenant_id),
    CONSTRAINT fk_roles_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
    role_id CHAR(36) NOT NULL,
    permission_id CHAR(36) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    email_verified_at DATETIME(6) NULL,
    last_login_at DATETIME(6) NULL,
    failed_login_count INT NOT NULL DEFAULT 0,
    locked_until DATETIME(6) NULL,
    mfa_enabled TINYINT(1) NOT NULL DEFAULT 0,
    mfa_secret_encrypted VARCHAR(512) NULL,
    password_changed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_tenant (tenant_id),
    KEY idx_users_status (status),
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auth_sessions (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NULL,
    refresh_token_hash CHAR(64) NOT NULL,
    family_id CHAR(36) NOT NULL,
    user_agent VARCHAR(512) NULL,
    ip_address VARCHAR(64) NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_sessions_refresh_hash (refresh_token_hash),
    KEY idx_auth_sessions_user (user_id),
    KEY idx_auth_sessions_family (family_id),
    CONSTRAINT fk_auth_sessions_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_tokens (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_password_reset_hash (token_hash),
    KEY idx_password_reset_user (user_id),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE email_verification_tokens (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_verify_hash (token_hash),
    KEY idx_email_verify_user (user_id),
    CONSTRAINT fk_email_verify_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NULL,
    user_id CHAR(36) NULL,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64) NULL,
    old_value JSON NULL,
    new_value JSON NULL,
    ip_address VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    correlation_id VARCHAR(64) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_audit_logs_tenant_created (tenant_id, created_at),
    KEY idx_audit_logs_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'USER_CREATE', 'Create users', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'USER_VIEW', 'View users', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'USER_UPDATE', 'Update users', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'USER_DELETE', 'Delete users', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'USER_DEACTIVATE', 'Activate or deactivate users', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'ROLE_VIEW', 'View roles', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'ROLE_ASSIGN', 'Assign roles', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'PERMISSION_VIEW', 'View permissions', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SESSION_VIEW', 'View sessions', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'SESSION_REVOKE', 'Revoke sessions', 'identity', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'TENANT_CREATE', 'Create tenants', 'tenant', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'TENANT_VIEW', 'View tenants', 'tenant', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'TENANT_UPDATE', 'Update tenants', 'tenant', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDIT_LOG_VIEW', 'View audit logs', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDIT_CREATE', 'Create audits', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDIT_VIEW', 'View audits', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDIT_UPDATE', 'Update audits', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDIT_DELETE', 'Delete audits', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDIT_ASSIGN', 'Assign auditors', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDIT_APPROVE', 'Approve audits', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CLIENT_CREATE', 'Create clients', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CLIENT_VIEW', 'View clients', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CLIENT_UPDATE', 'Update clients', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'DOCUMENT_UPLOAD', 'Upload documents', 'document', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'DOCUMENT_DOWNLOAD', 'Download documents', 'document', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'DOCUMENT_DELETE', 'Delete documents', 'document', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'FINDING_CREATE', 'Create findings', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'FINDING_UPDATE', 'Update findings', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'FINDING_CLOSE', 'Close findings', 'audit', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CERTIFICATE_ISSUE', 'Issue certificates', 'certification', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CERTIFICATE_SUSPEND', 'Suspend certificates', 'certification', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'CERTIFICATE_WITHDRAW', 'Withdraw certificates', 'certification', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'INVOICE_CREATE', 'Create invoices', 'finance', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'PAYMENT_RECORD', 'Record payments', 'finance', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'REPORT_EXPORT', 'Export reports', 'reporting', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'LEAD_VIEW', 'View leads', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'LEAD_CREATE', 'Create leads', 'crm', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDITOR_VIEW', 'View auditors', 'auditor', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'AUDITOR_UPDATE', 'Update auditors', 'auditor', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'TRAINING_VIEW', 'View training', 'training', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'COMPLAINT_VIEW', 'View complaints', 'governance', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'APPEAL_VIEW', 'View appeals', 'governance', NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), 'RISK_VIEW', 'View risks', 'governance', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO roles (id, tenant_id, code, name, description, system_role, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), NULL, 'PLATFORM_SUPER_ADMIN', 'Platform Super Admin', 'Operates the SaaS platform across tenants', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'TENANT_ADMIN', 'Tenant Admin', 'Administers one certification body tenant', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'CERTIFICATION_MANAGER', 'Certification Manager', 'Owns certification programmes and decisions support', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'AUDIT_MANAGER', 'Audit Manager', 'Plans and oversees audit delivery', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'LEAD_AUDITOR', 'Lead Auditor', 'Leads audit teams and issues findings', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'AUDITOR', 'Auditor', 'Executes assigned audits', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'TECHNICAL_REVIEWER', 'Technical Reviewer', 'Reviews audit packs before decision', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'CERTIFICATION_DECISION_MAKER', 'Certification Decision Maker', 'Issues, suspends, or withdraws certificates', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'ACCOUNTANT', 'Accountant', 'Invoices and payments', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'SALES_MANAGER', 'Sales Manager', 'Owns pipeline', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'SALES_EXECUTIVE', 'Sales Executive', 'Works leads and clients', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'DOCUMENT_CONTROLLER', 'Document Controller', 'Controls controlled documents', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'HR_COMPETENCY_MANAGER', 'HR / Competency Manager', 'Auditor competency and training', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'CLIENT_ADMIN', 'Client Admin', 'Client organisation administrator', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'CLIENT_USER', 'Client User', 'Client organisation user', 1, NOW(6), NOW(6), 'system', 'system', 0),
(UUID(), NULL, 'READ_ONLY', 'Read Only', 'View access without mutation', 1, NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.code = 'PLATFORM_SUPER_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'TENANT_ADMIN' AND p.code NOT IN ('TENANT_CREATE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'AUDIT_VIEW', 'AUDIT_APPROVE', 'FINDING_UPDATE', 'FINDING_CLOSE',
    'CERTIFICATE_ISSUE', 'CERTIFICATE_SUSPEND', 'CERTIFICATE_WITHDRAW',
    'CLIENT_VIEW', 'USER_VIEW', 'ROLE_VIEW', 'REPORT_EXPORT'
) WHERE r.code = 'CERTIFICATION_MANAGER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'AUDIT_CREATE', 'AUDIT_VIEW', 'AUDIT_UPDATE', 'AUDIT_DELETE', 'AUDIT_ASSIGN', 'AUDIT_APPROVE',
    'FINDING_CREATE', 'FINDING_UPDATE', 'FINDING_CLOSE', 'AUDITOR_VIEW', 'CLIENT_VIEW', 'USER_VIEW', 'REPORT_EXPORT'
) WHERE r.code = 'AUDIT_MANAGER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'AUDIT_VIEW', 'AUDIT_UPDATE', 'AUDIT_ASSIGN', 'FINDING_CREATE', 'FINDING_UPDATE', 'FINDING_CLOSE',
    'DOCUMENT_UPLOAD', 'DOCUMENT_DOWNLOAD', 'CLIENT_VIEW'
) WHERE r.code = 'LEAD_AUDITOR';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'AUDIT_VIEW', 'FINDING_CREATE', 'FINDING_UPDATE', 'DOCUMENT_UPLOAD', 'DOCUMENT_DOWNLOAD'
) WHERE r.code = 'AUDITOR';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'AUDIT_VIEW', 'AUDIT_APPROVE', 'FINDING_UPDATE', 'CLIENT_VIEW', 'REPORT_EXPORT'
) WHERE r.code = 'TECHNICAL_REVIEWER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'AUDIT_VIEW', 'CERTIFICATE_ISSUE', 'CERTIFICATE_SUSPEND', 'CERTIFICATE_WITHDRAW', 'CLIENT_VIEW'
) WHERE r.code = 'CERTIFICATION_DECISION_MAKER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'INVOICE_CREATE', 'PAYMENT_RECORD', 'CLIENT_VIEW', 'REPORT_EXPORT'
) WHERE r.code = 'ACCOUNTANT';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'LEAD_VIEW', 'LEAD_CREATE', 'CLIENT_VIEW', 'CLIENT_CREATE', 'CLIENT_UPDATE', 'REPORT_EXPORT'
) WHERE r.code = 'SALES_MANAGER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'LEAD_VIEW', 'LEAD_CREATE', 'CLIENT_VIEW', 'CLIENT_CREATE'
) WHERE r.code = 'SALES_EXECUTIVE';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'DOCUMENT_UPLOAD', 'DOCUMENT_DOWNLOAD', 'DOCUMENT_DELETE', 'CLIENT_VIEW', 'AUDIT_VIEW'
) WHERE r.code = 'DOCUMENT_CONTROLLER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'AUDITOR_VIEW', 'AUDITOR_UPDATE', 'TRAINING_VIEW', 'USER_VIEW'
) WHERE r.code = 'HR_COMPETENCY_MANAGER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'AUDIT_VIEW', 'FINDING_CREATE', 'FINDING_UPDATE', 'DOCUMENT_DOWNLOAD', 'CLIENT_VIEW'
) WHERE r.code = 'CLIENT_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
    'AUDIT_VIEW', 'DOCUMENT_DOWNLOAD'
) WHERE r.code = 'CLIENT_USER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code LIKE '%_VIEW'
WHERE r.code = 'READ_ONLY';
