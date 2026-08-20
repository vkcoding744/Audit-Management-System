package com.auditplatform.training.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAssessmentRequest(
        @NotBlank String auditorId,
        @NotNull LocalDate assessedOn,
        @Size(max = 255) String assessorName,
        String standardId,
        String schemeId,
        String competencyId,
        String notes
) {
}
