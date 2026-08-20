package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.AssignmentRole;
import com.auditplatform.audit.domain.AuditAssignment;

public record AssignmentResponse(
        String id,
        String tenantId,
        String auditId,
        String auditorId,
        AssignmentRole assignmentRole
) {
    public static AssignmentResponse from(AuditAssignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getTenantId(),
                assignment.getAuditId(),
                assignment.getAuditorId(),
                assignment.getAssignmentRole()
        );
    }
}
