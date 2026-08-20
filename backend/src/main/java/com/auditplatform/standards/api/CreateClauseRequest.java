package com.auditplatform.standards.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClauseRequest(
        String parentId,
        @NotBlank @Size(max = 64) String clauseCode,
        @NotBlank @Size(max = 255) String title,
        String requirementText,
        Integer sortOrder
) {
}
