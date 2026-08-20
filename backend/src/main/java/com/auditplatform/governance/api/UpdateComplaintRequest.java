package com.auditplatform.governance.api;

import com.auditplatform.governance.domain.ComplaintSource;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateComplaintRequest(
        @Size(max = 255) String subject,
        ComplaintSource source,
        LocalDate receivedOn,
        String description
) {
}
