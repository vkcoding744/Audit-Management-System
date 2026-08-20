package com.auditplatform.governance.api;

import com.auditplatform.governance.domain.Risk;
import com.auditplatform.governance.domain.RiskCategory;
import com.auditplatform.governance.domain.RiskStatus;

import java.time.LocalDate;

public record RiskResponse(
        String id,
        String tenantId,
        String riskNumber,
        String title,
        RiskCategory category,
        Integer likelihood,
        Integer impact,
        Integer score,
        RiskStatus status,
        String description,
        String mitigation,
        LocalDate closedOn
) {
    public static RiskResponse from(Risk risk) {
        Integer score = risk.getLikelihood() != null && risk.getImpact() != null
                ? risk.getLikelihood() * risk.getImpact()
                : null;
        return new RiskResponse(
                risk.getId(),
                risk.getTenantId(),
                risk.getRiskNumber(),
                risk.getTitle(),
                risk.getCategory(),
                risk.getLikelihood(),
                risk.getImpact(),
                score,
                risk.getStatus(),
                risk.getDescription(),
                risk.getMitigation(),
                risk.getClosedOn()
        );
    }
}
