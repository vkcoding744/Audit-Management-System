package com.auditplatform.standards.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "scheme_standards")
@Getter
@Setter
public class SchemeStandard extends TenantAwareEntity {

    @Column(name = "scheme_id", nullable = false, length = 36)
    private String schemeId;

    @Column(name = "standard_id", nullable = false, length = 36)
    private String standardId;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
