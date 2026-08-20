package com.auditplatform.training.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTrainingRecordRequest(
        @Size(max = 255) String title,
        @Size(max = 255) String provider,
        LocalDate plannedOn,
        LocalDate completedOn,
        @Min(0) Integer hours,
        LocalDate expiresOn,
        String standardId,
        String schemeId,
        String notes
) {
}
