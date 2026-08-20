package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditStage;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.AuditType;

import java.time.LocalDate;
import java.util.List;

public record AuditResponse(
        String id,
        String tenantId,
        String auditNumber,
        String programmeId,
        String clientId,
        String schemeId,
        String standardId,
        String checklistId,
        String name,
        AuditType auditType,
        AuditStage stage,
        AuditStatus status,
        LocalDate plannedStartOn,
        LocalDate plannedEndOn,
        LocalDate actualStartOn,
        LocalDate actualEndOn,
        String notes,
        String openingNotes,
        String closingNotes,
        List<AuditSiteResponse> sites,
        List<AssignmentResponse> assignments
) {
    public static AuditResponse from(Audit audit, List<AuditSiteResponse> sites, List<AssignmentResponse> assignments) {
        return new AuditResponse(
                audit.getId(),
                audit.getTenantId(),
                audit.getAuditNumber(),
                audit.getProgrammeId(),
                audit.getClientId(),
                audit.getSchemeId(),
                audit.getStandardId(),
                audit.getChecklistId(),
                audit.getName(),
                audit.getAuditType(),
                audit.getStage(),
                audit.getStatus(),
                audit.getPlannedStartOn(),
                audit.getPlannedEndOn(),
                audit.getActualStartOn(),
                audit.getActualEndOn(),
                audit.getNotes(),
                audit.getOpeningNotes(),
                audit.getClosingNotes(),
                sites,
                assignments
        );
    }

    public static AuditResponse summary(Audit audit) {
        return from(audit, List.of(), List.of());
    }
}
