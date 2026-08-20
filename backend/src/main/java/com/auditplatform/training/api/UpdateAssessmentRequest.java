package com.auditplatform.training.api;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateAssessmentRequest(
        LocalDate assessedOn,
        @Size(max = 255) String assessorName,
        String standardId,
        String schemeId,
        String competencyId,
        String notes
) {
}
