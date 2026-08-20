package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.StandardClause;

public record ClauseResponse(
        String id,
        String tenantId,
        String standardId,
        String parentId,
        String clauseCode,
        String title,
        String requirementText,
        int sortOrder
) {
    public static ClauseResponse from(StandardClause clause) {
        return new ClauseResponse(
                clause.getId(),
                clause.getTenantId(),
                clause.getStandardId(),
                clause.getParentId(),
                clause.getClauseCode(),
                clause.getTitle(),
                clause.getRequirementText(),
                clause.getSortOrder()
        );
    }
}
