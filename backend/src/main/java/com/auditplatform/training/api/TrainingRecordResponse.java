package com.auditplatform.training.api;

import com.auditplatform.training.domain.TrainingRecord;
import com.auditplatform.training.domain.TrainingStatus;

import java.time.LocalDate;

public record TrainingRecordResponse(
        String id,
        String tenantId,
        String trainingNumber,
        String auditorId,
        String title,
        String provider,
        LocalDate plannedOn,
        LocalDate completedOn,
        Integer hours,
        LocalDate expiresOn,
        String standardId,
        String schemeId,
        TrainingStatus status,
        boolean expired,
        String notes
) {
    public static TrainingRecordResponse from(TrainingRecord record, boolean expired) {
        return new TrainingRecordResponse(
                record.getId(),
                record.getTenantId(),
                record.getTrainingNumber(),
                record.getAuditorId(),
                record.getTitle(),
                record.getProvider(),
                record.getPlannedOn(),
                record.getCompletedOn(),
                record.getHours(),
                record.getExpiresOn(),
                record.getStandardId(),
                record.getSchemeId(),
                record.getStatus(),
                expired,
                record.getNotes()
        );
    }
}
