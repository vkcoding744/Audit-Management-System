package com.auditplatform.governance.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateImpartialityRequest(
        @NotBlank @Size(max = 255) String title,
        String auditorId,
        String clientId,
        LocalDate identifiedOn,
        String description
) {
}
