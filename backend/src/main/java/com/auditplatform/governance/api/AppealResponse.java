package com.auditplatform.governance.api;

import com.auditplatform.governance.domain.Appeal;
import com.auditplatform.governance.domain.AppealOutcome;
import com.auditplatform.governance.domain.AppealStatus;

import java.time.LocalDate;

public record AppealResponse(
        String id,
        String tenantId,
        String appealNumber,
        String clientId,
        String certificateId,
        String findingId,
        String subject,
        LocalDate receivedOn,
        AppealStatus status,
        AppealOutcome outcome,
        String description,
        String decisionNotes,
        LocalDate decidedOn
) {
    public static AppealResponse from(Appeal appeal) {
        return new AppealResponse(
                appeal.getId(),
                appeal.getTenantId(),
                appeal.getAppealNumber(),
                appeal.getClientId(),
                appeal.getCertificateId(),
                appeal.getFindingId(),
                appeal.getSubject(),
                appeal.getReceivedOn(),
                appeal.getStatus(),
                appeal.getOutcome(),
                appeal.getDescription(),
                appeal.getDecisionNotes(),
                appeal.getDecidedOn()
        );
    }
}
