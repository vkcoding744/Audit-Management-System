CREATE TABLE quotes (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    quote_number VARCHAR(32) NOT NULL,
    client_id CHAR(36) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    valid_until DATE NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_quotes_tenant_number (tenant_id, quote_number),
    KEY idx_quotes_tenant_client_status (tenant_id, client_id, status),
    CONSTRAINT fk_quotes_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_quotes_client FOREIGN KEY (client_id) REFERENCES clients (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quote_lines (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    quote_id CHAR(36) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    unit_amount DECIMAL(15,2) NOT NULL,
    line_amount DECIMAL(15,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_quote_lines_quote (quote_id),
    CONSTRAINT fk_quote_lines_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_quote_lines_quote FOREIGN KEY (quote_id) REFERENCES quotes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE invoices (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    invoice_number VARCHAR(32) NOT NULL,
    client_id CHAR(36) NOT NULL,
    quote_id CHAR(36) NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    issued_on DATE NULL,
    due_on DATE NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    amount_paid DECIMAL(15,2) NOT NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_invoices_tenant_number (tenant_id, invoice_number),
    UNIQUE KEY uk_invoices_quote (quote_id),
    KEY idx_invoices_tenant_client_status (tenant_id, client_id, status),
    KEY idx_invoices_tenant_due (tenant_id, status, due_on),
    CONSTRAINT fk_invoices_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_invoices_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_invoices_quote FOREIGN KEY (quote_id) REFERENCES quotes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE invoice_lines (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    invoice_id CHAR(36) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    unit_amount DECIMAL(15,2) NOT NULL,
    line_amount DECIMAL(15,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_invoice_lines_invoice (invoice_id),
    CONSTRAINT fk_invoice_lines_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_invoice_lines_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    payment_number VARCHAR(32) NOT NULL,
    invoice_id CHAR(36) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    paid_on DATE NOT NULL,
    method VARCHAR(32) NOT NULL,
    reference VARCHAR(64) NULL,
    notes TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    deleted_at DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_tenant_number (tenant_id, payment_number),
    KEY idx_payments_invoice (invoice_id),
    CONSTRAINT fk_payments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO permissions (id, code, name, module, created_at, updated_at, created_by, updated_by, version) VALUES
(UUID(), 'INVOICE_VIEW', 'View invoices and quotes', 'finance', NOW(6), NOW(6), 'system', 'system', 0);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code = 'INVOICE_VIEW'
WHERE r.code IN (
    'PLATFORM_SUPER_ADMIN',
    'TENANT_ADMIN',
    'ACCOUNTANT',
    'SALES_MANAGER',
    'SALES_EXECUTIVE',
    'CERTIFICATION_MANAGER',
    'CLIENT_ADMIN',
    'CLIENT_USER',
    'READ_ONLY'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
