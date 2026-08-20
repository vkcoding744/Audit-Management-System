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
@Table(name = "certification_decisions")
@Getter
@Setter
public class CertificationDecision extends TenantAwareEntity {

    @Column(name = "certificate_id", nullable = false, length = 36)
    private String certificateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false, length = 32)
    private DecisionType decisionType;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "decided_on", nullable = false)
    private LocalDate decidedOn;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
