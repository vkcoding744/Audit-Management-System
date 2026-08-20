package com.auditplatform.audit.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateCapaRequest(
        @NotBlank String description,
        @NotNull LocalDate dueOn,
        String notes
) {
}
