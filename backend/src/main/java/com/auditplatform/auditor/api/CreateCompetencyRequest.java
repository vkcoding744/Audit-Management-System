package com.auditplatform.auditor.api;

import com.auditplatform.auditor.domain.CompetencyRole;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateCompetencyRequest(
        String standardId,
        String schemeId,
        CompetencyRole competencyRole,
        @NotNull LocalDate validFrom,
        LocalDate validTo,
        String notes
) {
}
