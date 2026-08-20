package com.auditplatform.certification.api;

import com.auditplatform.certification.domain.CertificateSurveillance;
import com.auditplatform.certification.domain.SurveillanceStatus;

import java.time.LocalDate;

public record SurveillanceResponse(
        String id,
        String tenantId,
        String certificateId,
        LocalDate plannedOn,
        LocalDate completedOn,
        SurveillanceStatus status,
        String notes
) {
    public static SurveillanceResponse from(CertificateSurveillance row) {
        return new SurveillanceResponse(
                row.getId(),
                row.getTenantId(),
                row.getCertificateId(),
                row.getPlannedOn(),
                row.getCompletedOn(),
                row.getStatus(),
                row.getNotes()
        );
    }
}
