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
@Table(name = "competency_assessments")
@Getter
@Setter
public class CompetencyAssessment extends TenantAwareEntity {

    @Column(name = "assessment_number", nullable = false, length = 32)
    private String assessmentNumber;

    @Column(name = "auditor_id", nullable = false, length = 36)
    private String auditorId;

    @Column(name = "competency_id", length = 36)
    private String competencyId;

    @Column(name = "standard_id", length = 36)
    private String standardId;

    @Column(name = "scheme_id", length = 36)
    private String schemeId;

    @Column(name = "assessed_on", nullable = false)
    private LocalDate assessedOn;

    @Column(name = "assessor_name", length = 255)
    private String assessorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 32)
    private AssessmentResult result;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AssessmentStatus status = AssessmentStatus.DRAFT;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
