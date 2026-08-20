package com.auditplatform.governance.api;

import com.auditplatform.governance.domain.AppealOutcome;
import jakarta.validation.constraints.NotNull;

public record DecideAppealRequest(
        @NotNull AppealOutcome outcome,
        String notes
) {
}
