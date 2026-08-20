package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.Finding;
import com.auditplatform.audit.domain.FindingSeverity;
import com.auditplatform.audit.domain.FindingStatus;

import java.time.LocalDate;
import java.util.List;

public record FindingResponse(
        String id,
        String tenantId,
        String findingNumber,
        String auditId,
        String clientId,
        String siteId,
        String responseId,
        String clauseId,
        String title,
        String description,
        FindingSeverity severity,
        FindingStatus status,
        LocalDate closedOn,
        String notes,
        List<CapaResponse> capa
) {
    public static FindingResponse from(Finding finding, List<CapaResponse> capa) {
        return new FindingResponse(
                finding.getId(),
                finding.getTenantId(),
                finding.getFindingNumber(),
                finding.getAuditId(),
                finding.getClientId(),
                finding.getSiteId(),
                finding.getResponseId(),
                finding.getClauseId(),
                finding.getTitle(),
                finding.getDescription(),
                finding.getSeverity(),
                finding.getStatus(),
                finding.getClosedOn(),
                finding.getNotes(),
                capa
        );
    }

    public static FindingResponse summary(Finding finding) {
        return from(finding, List.of());
    }
}
