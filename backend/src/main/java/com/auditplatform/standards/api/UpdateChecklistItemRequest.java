package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.ChecklistItemType;
import jakarta.validation.constraints.Size;

public record UpdateChecklistItemRequest(
        String clauseId,
        @Size(max = 500) String title,
        String guidance,
        ChecklistItemType itemType,
        Boolean required,
        Integer sortOrder
) {
}
