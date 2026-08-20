package com.auditplatform.audit.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_sites")
@Getter
@Setter
public class AuditSite extends TenantAwareEntity {

    @Column(name = "audit_id", nullable = false, length = 36)
    private String auditId;

    @Column(name = "site_id", nullable = false, length = 36)
    private String siteId;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
