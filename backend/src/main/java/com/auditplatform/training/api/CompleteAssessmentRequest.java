package com.auditplatform.training.api;

import com.auditplatform.training.domain.AssessmentResult;
import jakarta.validation.constraints.NotNull;

public record CompleteAssessmentRequest(
        @NotNull AssessmentResult result,
        String notes
) {
}
