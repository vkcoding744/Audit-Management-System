package com.auditplatform.auditor.api;

import com.auditplatform.auditor.domain.AuditorCompetency;
import com.auditplatform.auditor.domain.CompetencyRole;
import com.auditplatform.auditor.domain.CompetencyStatus;

import java.time.LocalDate;

public record CompetencyResponse(
        String id,
        String tenantId,
        String auditorId,
        String standardId,
        String schemeId,
        CompetencyRole competencyRole,
        CompetencyStatus status,
        LocalDate validFrom,
        LocalDate validTo,
        boolean expired,
        boolean current,
        String notes
) {
    public static CompetencyResponse from(AuditorCompetency competency, LocalDate on) {
        return new CompetencyResponse(
                competency.getId(),
                competency.getTenantId(),
                competency.getAuditorId(),
                competency.getStandardId(),
                competency.getSchemeId(),
                competency.getCompetencyRole(),
                competency.getStatus(),
                competency.getValidFrom(),
                competency.getValidTo(),
                competency.isExpiredOn(on),
                competency.isCurrentOn(on),
                competency.getNotes()
        );
    }
}
