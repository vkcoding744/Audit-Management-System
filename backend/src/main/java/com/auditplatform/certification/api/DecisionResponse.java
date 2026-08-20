package com.auditplatform.certification.api;

import com.auditplatform.certification.domain.CertificationDecision;
import com.auditplatform.certification.domain.DecisionType;

import java.time.LocalDate;

public record DecisionResponse(
        String id,
        String tenantId,
        String certificateId,
        DecisionType decisionType,
        String reason,
        LocalDate decidedOn
) {
    public static DecisionResponse from(CertificationDecision decision) {
        return new DecisionResponse(
                decision.getId(),
                decision.getTenantId(),
                decision.getCertificateId(),
                decision.getDecisionType(),
                decision.getReason(),
                decision.getDecidedOn()
        );
    }
}
