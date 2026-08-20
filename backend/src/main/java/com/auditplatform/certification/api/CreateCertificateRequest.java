package com.auditplatform.certification.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateCertificateRequest(
        @NotBlank String auditId,
        LocalDate validFrom,
        @NotNull LocalDate expiresOn,
        String scopeText,
        LocalDate nextSurveillanceOn,
        String notes
) {
}
