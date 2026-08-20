package com.auditplatform.certification.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "certificates")
@Getter
@Setter
public class Certificate extends TenantAwareEntity {

    @Column(name = "certificate_number", nullable = false, length = 32)
    private String certificateNumber;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "scheme_id", nullable = false, length = 36)
    private String schemeId;

    @Column(name = "standard_id", length = 36)
    private String standardId;

    @Column(name = "programme_id", length = 36)
    private String programmeId;

    @Column(name = "audit_id", nullable = false, length = 36)
    private String auditId;

    @Column(name = "scope_text", columnDefinition = "TEXT")
    private String scopeText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CertificateStatus status = CertificateStatus.DRAFT;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "expires_on", nullable = false)
    private LocalDate expiresOn;

    @Column(name = "next_surveillance_on")
    private LocalDate nextSurveillanceOn;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
