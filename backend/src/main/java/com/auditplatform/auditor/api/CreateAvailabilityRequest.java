package com.auditplatform.auditor.api;

import com.auditplatform.auditor.domain.AvailabilityKind;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAvailabilityRequest(
        @NotNull LocalDate startOn,
        @NotNull LocalDate endOn,
        AvailabilityKind kind,
        @Size(max = 255) String reason
) {
}
