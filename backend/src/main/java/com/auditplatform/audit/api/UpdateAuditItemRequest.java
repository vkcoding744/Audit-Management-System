package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.AssessmentResult;
import jakarta.validation.constraints.NotNull;

public record UpdateAuditItemRequest(
        @NotNull AssessmentResult result,
        String comment
) {
}
