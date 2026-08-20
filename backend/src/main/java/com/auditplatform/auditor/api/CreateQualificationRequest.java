package com.auditplatform.auditor.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateQualificationRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 255) String issuer,
        LocalDate issuedOn,
        LocalDate expiresOn,
        String notes
) {
}
