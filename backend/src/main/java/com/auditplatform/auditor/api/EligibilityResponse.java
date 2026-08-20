package com.auditplatform.auditor.api;

import java.time.LocalDate;
import java.util.List;

public record EligibilityResponse(
        String auditorId,
        String standardId,
        String schemeId,
        LocalDate on,
        boolean eligible,
        List<String> reasons
) {
}
