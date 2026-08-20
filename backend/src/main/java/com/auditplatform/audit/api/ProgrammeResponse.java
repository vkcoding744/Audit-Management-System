package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.AuditProgramme;
import com.auditplatform.audit.domain.ProgrammeStatus;

import java.time.LocalDate;

public record ProgrammeResponse(
        String id,
        String tenantId,
        String programmeNumber,
        String clientId,
        String schemeId,
        String standardId,
        String name,
        ProgrammeStatus status,
        LocalDate cycleStartOn,
        LocalDate cycleEndOn,
        String notes
) {
    public static ProgrammeResponse from(AuditProgramme programme) {
        return new ProgrammeResponse(
                programme.getId(),
                programme.getTenantId(),
                programme.getProgrammeNumber(),
                programme.getClientId(),
                programme.getSchemeId(),
                programme.getStandardId(),
                programme.getName(),
                programme.getStatus(),
                programme.getCycleStartOn(),
                programme.getCycleEndOn(),
                programme.getNotes()
        );
    }
}
