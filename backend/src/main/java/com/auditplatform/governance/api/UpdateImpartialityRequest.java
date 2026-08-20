package com.auditplatform.governance.api;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateImpartialityRequest(
        @Size(max = 255) String title,
        LocalDate identifiedOn,
        String description,
        String reviewNotes
) {
}
