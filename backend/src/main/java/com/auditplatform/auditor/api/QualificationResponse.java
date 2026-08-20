package com.auditplatform.auditor.api;

import com.auditplatform.auditor.domain.AuditorQualification;

import java.time.LocalDate;

public record QualificationResponse(
        String id,
        String tenantId,
        String auditorId,
        String title,
        String issuer,
        LocalDate issuedOn,
        LocalDate expiresOn,
        String notes
) {
    public static QualificationResponse from(AuditorQualification qualification) {
        return new QualificationResponse(
                qualification.getId(),
                qualification.getTenantId(),
                qualification.getAuditorId(),
                qualification.getTitle(),
                qualification.getIssuer(),
                qualification.getIssuedOn(),
                qualification.getExpiresOn(),
                qualification.getNotes()
        );
    }
}
