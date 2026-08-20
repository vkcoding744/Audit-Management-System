package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.ChecklistStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChecklistRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 32) String versionLabel,
        String standardId,
        ChecklistStatus status,
        String notes
) {
}
