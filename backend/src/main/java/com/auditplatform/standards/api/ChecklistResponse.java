package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.Checklist;
import com.auditplatform.standards.domain.ChecklistStatus;

import java.util.List;

public record ChecklistResponse(
        String id,
        String tenantId,
        String schemeId,
        String standardId,
        String name,
        String versionLabel,
        ChecklistStatus status,
        String notes,
        List<ChecklistItemResponse> items
) {
    public static ChecklistResponse from(Checklist checklist, List<ChecklistItemResponse> items) {
        return new ChecklistResponse(
                checklist.getId(),
                checklist.getTenantId(),
                checklist.getSchemeId(),
                checklist.getStandardId(),
                checklist.getName(),
                checklist.getVersionLabel(),
                checklist.getStatus(),
                checklist.getNotes(),
                items
        );
    }

    public static ChecklistResponse summary(Checklist checklist) {
        return from(checklist, List.of());
    }
}
