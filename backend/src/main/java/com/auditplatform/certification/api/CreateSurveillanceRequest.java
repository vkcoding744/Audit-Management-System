package com.auditplatform.certification.api;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSurveillanceRequest(
        @NotNull LocalDate plannedOn,
        String notes
) {
}
