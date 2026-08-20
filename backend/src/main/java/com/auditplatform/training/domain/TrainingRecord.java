package com.auditplatform.training.domain;

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
@Table(name = "training_records")
@Getter
@Setter
public class TrainingRecord extends TenantAwareEntity {

    @Column(name = "training_number", nullable = false, length = 32)
    private String trainingNumber;

    @Column(name = "auditor_id", nullable = false, length = 36)
    private String auditorId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "provider", length = 255)
    private String provider;

    @Column(name = "planned_on")
    private LocalDate plannedOn;

    @Column(name = "completed_on")
    private LocalDate completedOn;

    @Column(name = "hours")
    private Integer hours;

    @Column(name = "expires_on")
    private LocalDate expiresOn;

    @Column(name = "standard_id", length = 36)
    private String standardId;

    @Column(name = "scheme_id", length = 36)
    private String schemeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TrainingStatus status = TrainingStatus.PLANNED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
