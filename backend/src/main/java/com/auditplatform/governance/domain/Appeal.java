package com.auditplatform.governance.domain;

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
@Table(name = "appeals")
@Getter
@Setter
public class Appeal extends TenantAwareEntity {

    @Column(name = "appeal_number", nullable = false, length = 32)
    private String appealNumber;

    @Column(name = "client_id", length = 36)
    private String clientId;

    @Column(name = "certificate_id", length = 36)
    private String certificateId;

    @Column(name = "finding_id", length = 36)
    private String findingId;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "received_on", nullable = false)
    private LocalDate receivedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AppealStatus status = AppealStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 32)
    private AppealOutcome outcome;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "decision_notes", columnDefinition = "TEXT")
    private String decisionNotes;

    @Column(name = "decided_on")
    private LocalDate decidedOn;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
