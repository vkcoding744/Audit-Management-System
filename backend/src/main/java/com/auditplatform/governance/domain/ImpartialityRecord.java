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
@Table(name = "impartiality_records")
@Getter
@Setter
public class ImpartialityRecord extends TenantAwareEntity {

    @Column(name = "impartiality_number", nullable = false, length = 32)
    private String impartialityNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "auditor_id", length = 36)
    private String auditorId;

    @Column(name = "client_id", length = 36)
    private String clientId;

    @Column(name = "identified_on", nullable = false)
    private LocalDate identifiedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ImpartialityStatus status = ImpartialityStatus.OPEN;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "closed_on")
    private LocalDate closedOn;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
