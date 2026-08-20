package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.AssignmentRole;
import jakarta.validation.constraints.NotBlank;

public record CreateAssignmentRequest(
        @NotBlank String auditorId,
        AssignmentRole assignmentRole
) {
}
