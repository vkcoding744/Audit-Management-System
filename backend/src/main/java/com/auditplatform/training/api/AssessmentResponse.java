package com.auditplatform.training.api;

import com.auditplatform.training.domain.AssessmentResult;
import com.auditplatform.training.domain.AssessmentStatus;
import com.auditplatform.training.domain.CompetencyAssessment;

import java.time.LocalDate;

public record AssessmentResponse(
        String id,
        String tenantId,
        String assessmentNumber,
        String auditorId,
        String competencyId,
        String standardId,
        String schemeId,
        LocalDate assessedOn,
        String assessorName,
        AssessmentResult result,
        AssessmentStatus status,
        String notes
) {
    public static AssessmentResponse from(CompetencyAssessment assessment) {
        return new AssessmentResponse(
                assessment.getId(),
                assessment.getTenantId(),
                assessment.getAssessmentNumber(),
                assessment.getAuditorId(),
                assessment.getCompetencyId(),
                assessment.getStandardId(),
                assessment.getSchemeId(),
                assessment.getAssessedOn(),
                assessment.getAssessorName(),
                assessment.getResult(),
                assessment.getStatus(),
                assessment.getNotes()
        );
    }
}
