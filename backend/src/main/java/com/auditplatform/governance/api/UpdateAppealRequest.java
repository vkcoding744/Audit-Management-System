package com.auditplatform.governance.api;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateAppealRequest(
        @Size(max = 255) String subject,
        LocalDate receivedOn,
        String description
) {
}
