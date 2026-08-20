package com.auditplatform.audit.api;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProgrammeRequest(
        @Size(max = 255) String name,
        LocalDate cycleStartOn,
        LocalDate cycleEndOn,
        String notes
) {
}
