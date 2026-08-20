package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.AssessmentResult;
import com.auditplatform.audit.domain.AuditChecklistResponse;
import com.auditplatform.standards.domain.ChecklistItemType;

import java.time.Instant;

public record AuditItemResponse(
        String id,
        String tenantId,
        String auditId,
        String checklistItemId,
        String clauseId,
        String title,
        String guidance,
        ChecklistItemType itemType,
        boolean required,
        int sortOrder,
        AssessmentResult result,
        String comment,
        String assessedBy,
        Instant assessedAt
) {
    public static AuditItemResponse from(AuditChecklistResponse response) {
        return new AuditItemResponse(
                response.getId(),
                response.getTenantId(),
                response.getAuditId(),
                response.getChecklistItemId(),
                response.getClauseId(),
                response.getTitle(),
                response.getGuidance(),
                response.getItemType(),
                response.isRequired(),
                response.getSortOrder(),
                response.getResult(),
                response.getComment(),
                response.getAssessedBy(),
                response.getAssessedAt()
        );
    }
}
