package com.auditplatform.common.persistence;

import com.auditplatform.common.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(
        name = TenantAwareEntity.TENANT_FILTER,
        parameters = @ParamDef(name = "tenantId", type = String.class)
)
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = "tenant_id = :tenantId")
public abstract class TenantAwareEntity extends AuditableEntity {

    public static final String TENANT_FILTER = "tenantIsolation";

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
