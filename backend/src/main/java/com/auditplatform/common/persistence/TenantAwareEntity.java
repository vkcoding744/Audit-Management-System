package com.auditplatform.common.persistence;

import com.auditplatform.common.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

@MappedSuperclass
public abstract class TenantAwareEntity extends AuditableEntity {

    @Column(name = "tenant_id", length = 36, nullable = false, updatable = false)
    private String tenantId;

    @PrePersist
    protected void assignTenant() {
        if (tenantId == null) {
            String current = TenantContext.getTenantId();
            if (current == null || current.isBlank()) {
                throw new IllegalStateException("Tenant context is required to persist this entity");
            }
            tenantId = current;
        }
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
