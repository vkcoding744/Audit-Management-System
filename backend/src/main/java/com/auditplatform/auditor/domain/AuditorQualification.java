package com.auditplatform.auditor.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "auditor_qualifications")
@Getter
@Setter
public class AuditorQualification extends TenantAwareEntity {

    @Column(name = "auditor_id", nullable = false, length = 36)
    private String auditorId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "issued_on")
    private LocalDate issuedOn;

    @Column(name = "expires_on")
    private LocalDate expiresOn;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
