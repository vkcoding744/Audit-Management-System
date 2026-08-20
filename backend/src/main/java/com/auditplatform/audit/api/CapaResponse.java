package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.CapaAction;
import com.auditplatform.audit.domain.CapaStatus;

import java.time.LocalDate;

public record CapaResponse(
        String id,
        String tenantId,
        String capaNumber,
        String findingId,
        String description,
        LocalDate dueOn,
        LocalDate completedOn,
        CapaStatus status,
        String notes
) {
    public static CapaResponse from(CapaAction capa) {
        return new CapaResponse(
                capa.getId(),
                capa.getTenantId(),
                capa.getCapaNumber(),
                capa.getFindingId(),
                capa.getDescription(),
                capa.getDueOn(),
                capa.getCompletedOn(),
                capa.getStatus(),
                capa.getNotes()
        );
    }
}
