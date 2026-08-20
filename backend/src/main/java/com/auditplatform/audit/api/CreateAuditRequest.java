package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.AuditStage;
import com.auditplatform.audit.domain.AuditType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAuditRequest(
        @NotBlank String programmeId,
        @NotBlank @Size(max = 255) String name,
        AuditType auditType,
        AuditStage stage,
        String checklistId,
        LocalDate plannedStartOn,
        LocalDate plannedEndOn,
        String notes
) {
}
