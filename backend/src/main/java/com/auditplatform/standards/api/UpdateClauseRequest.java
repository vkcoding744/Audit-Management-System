package com.auditplatform.standards.api;

import jakarta.validation.constraints.Size;

public record UpdateClauseRequest(
        String parentId,
        @Size(max = 64) String clauseCode,
        @Size(max = 255) String title,
        String requirementText,
        Integer sortOrder
) {
}
