INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'DASHBOARD_VIEW', 'View tenant operational dashboard', 'dashboard', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code = 'DASHBOARD_VIEW'
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'CERTIFICATION_MANAGER',
    'AUDIT_MANAGER',
    'TECHNICAL_REVIEWER',
    'ACCOUNTANT',
    'SALES_MANAGER',
    'LEAD_AUDITOR',
    'READ_ONLY'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
