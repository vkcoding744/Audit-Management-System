package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.ChecklistStatus;
import jakarta.validation.constraints.Size;

public record UpdateChecklistRequest(
        @Size(max = 255) String name,
        @Size(max = 32) String versionLabel,
        String standardId,
        ChecklistStatus status,
        String notes
) {
}
