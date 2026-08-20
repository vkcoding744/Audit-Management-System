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
@Table(name = "certificate_surveillance")
@Getter
@Setter
public class CertificateSurveillance extends TenantAwareEntity {

    @Column(name = "certificate_id", nullable = false, length = 36)
    private String certificateId;

    @Column(name = "planned_on", nullable = false)
    private LocalDate plannedOn;

    @Column(name = "completed_on")
    private LocalDate completedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SurveillanceStatus status = SurveillanceStatus.PLANNED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
