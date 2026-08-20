package com.auditplatform.governance.api;

import com.auditplatform.governance.domain.ImpartialityRecord;
import com.auditplatform.governance.domain.ImpartialityStatus;

import java.time.LocalDate;

public record ImpartialityResponse(
        String id,
        String tenantId,
        String impartialityNumber,
        String title,
        String auditorId,
        String clientId,
        LocalDate identifiedOn,
        ImpartialityStatus status,
        String description,
        String reviewNotes,
        LocalDate closedOn
) {
    public static ImpartialityResponse from(ImpartialityRecord record) {
        return new ImpartialityResponse(
                record.getId(),
                record.getTenantId(),
                record.getImpartialityNumber(),
                record.getTitle(),
                record.getAuditorId(),
                record.getClientId(),
                record.getIdentifiedOn(),
                record.getStatus(),
                record.getDescription(),
                record.getReviewNotes(),
                record.getClosedOn()
        );
    }
}
