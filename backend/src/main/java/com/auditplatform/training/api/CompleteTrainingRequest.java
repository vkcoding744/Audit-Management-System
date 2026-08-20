package com.auditplatform.training.api;

import java.time.LocalDate;

public record CompleteTrainingRequest(
        LocalDate completedOn,
        String notes
) {
}
