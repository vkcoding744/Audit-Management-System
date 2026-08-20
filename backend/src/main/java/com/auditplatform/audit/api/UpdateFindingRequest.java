package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.FindingSeverity;
import jakarta.validation.constraints.Size;

public record UpdateFindingRequest(
        @Size(max = 255) String title,
        String description,
        FindingSeverity severity,
        String notes
) {
}
