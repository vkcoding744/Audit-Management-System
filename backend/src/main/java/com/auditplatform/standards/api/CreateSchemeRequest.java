package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.SchemeStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSchemeRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 255) String name,
        String description,
        @Size(max = 255) String accreditationBody,
        @Min(1) Integer cycleMonths,
        @Min(1) Integer surveillanceIntervalMonths,
        SchemeStatus status,
        String notes
) {
}
