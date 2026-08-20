package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.ChecklistItem;
import com.auditplatform.standards.domain.ChecklistItemType;

public record ChecklistItemResponse(
        String id,
        String tenantId,
        String checklistId,
        String clauseId,
        String title,
        String guidance,
        ChecklistItemType itemType,
        boolean required,
        int sortOrder
) {
    public static ChecklistItemResponse from(ChecklistItem item) {
        return new ChecklistItemResponse(
                item.getId(),
                item.getTenantId(),
                item.getChecklistId(),
                item.getClauseId(),
                item.getTitle(),
                item.getGuidance(),
                item.getItemType(),
                item.isRequired(),
                item.getSortOrder()
        );
    }
}
