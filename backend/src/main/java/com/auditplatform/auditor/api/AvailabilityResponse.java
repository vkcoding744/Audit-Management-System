package com.auditplatform.auditor.api;

import com.auditplatform.auditor.domain.AuditorAvailability;
import com.auditplatform.auditor.domain.AvailabilityKind;

import java.time.LocalDate;

public record AvailabilityResponse(
        String id,
        String tenantId,
        String auditorId,
        LocalDate startOn,
        LocalDate endOn,
        AvailabilityKind kind,
        String reason
) {
    public static AvailabilityResponse from(AuditorAvailability availability) {
        return new AvailabilityResponse(
                availability.getId(),
                availability.getTenantId(),
                availability.getAuditorId(),
                availability.getStartOn(),
                availability.getEndOn(),
                availability.getKind(),
                availability.getReason()
        );
    }
}
