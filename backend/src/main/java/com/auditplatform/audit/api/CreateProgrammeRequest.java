package com.auditplatform.audit.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProgrammeRequest(
        @NotBlank String clientId,
        @NotBlank String schemeId,
        String standardId,
        @NotBlank @Size(max = 255) String name,
        LocalDate cycleStartOn,
        LocalDate cycleEndOn,
        String notes
) {
}
