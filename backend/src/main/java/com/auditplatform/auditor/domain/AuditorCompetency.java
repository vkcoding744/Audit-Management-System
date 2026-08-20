package com.auditplatform.auditor.domain;

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
@Table(name = "auditor_competencies")
@Getter
@Setter
public class AuditorCompetency extends TenantAwareEntity {

    @Column(name = "auditor_id", nullable = false, length = 36)
    private String auditorId;

    @Column(name = "standard_id", length = 36)
    private String standardId;

    @Column(name = "scheme_id", length = 36)
    private String schemeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "competency_role", nullable = false, length = 32)
    private CompetencyRole competencyRole = CompetencyRole.TEAM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CompetencyStatus status = CompetencyStatus.ACTIVE;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean covers(String standardId, String schemeId) {
        if (standardId != null && standardId.equals(this.standardId)) {
            return true;
        }
        return schemeId != null && schemeId.equals(this.schemeId);
    }

    public boolean isCurrentOn(LocalDate on) {
        if (status != CompetencyStatus.ACTIVE) {
            return false;
        }
        if (validFrom != null && on.isBefore(validFrom)) {
            return false;
        }
        return validTo == null || !on.isAfter(validTo);
    }

    public boolean isExpiredOn(LocalDate on) {
        return validTo != null && on.isAfter(validTo);
    }
}
