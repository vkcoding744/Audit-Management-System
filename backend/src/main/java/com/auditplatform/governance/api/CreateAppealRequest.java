package com.auditplatform.governance.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAppealRequest(
        String clientId,
        String certificateId,
        String findingId,
        @NotBlank @Size(max = 255) String subject,
        LocalDate receivedOn,
        String description
) {
}
