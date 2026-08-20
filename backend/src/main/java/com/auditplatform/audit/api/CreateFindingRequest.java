package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.FindingSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFindingRequest(
        @NotBlank String auditId,
        @NotBlank @Size(max = 255) String title,
        @NotBlank String description,
        FindingSeverity severity,
        String siteId,
        String responseId,
        String clauseId,
        String notes
) {
}
