package com.auditplatform.certification.api;

import com.auditplatform.certification.domain.Certificate;
import com.auditplatform.certification.domain.CertificateStatus;

import java.time.LocalDate;
import java.util.List;

public record CertificateResponse(
        String id,
        String tenantId,
        String certificateNumber,
        String clientId,
        String schemeId,
        String standardId,
        String programmeId,
        String auditId,
        String scopeText,
        CertificateStatus status,
        LocalDate validFrom,
        LocalDate expiresOn,
        LocalDate nextSurveillanceOn,
        boolean expired,
        String notes,
        List<DecisionResponse> decisions,
        List<SurveillanceResponse> surveillance
) {
    public static CertificateResponse from(
            Certificate certificate,
            boolean expired,
            List<DecisionResponse> decisions,
            List<SurveillanceResponse> surveillance
    ) {
        return new CertificateResponse(
                certificate.getId(),
                certificate.getTenantId(),
                certificate.getCertificateNumber(),
                certificate.getClientId(),
                certificate.getSchemeId(),
                certificate.getStandardId(),
                certificate.getProgrammeId(),
                certificate.getAuditId(),
                certificate.getScopeText(),
                certificate.getStatus(),
                certificate.getValidFrom(),
                certificate.getExpiresOn(),
                certificate.getNextSurveillanceOn(),
                expired,
                certificate.getNotes(),
                decisions,
                surveillance
        );
    }
}
