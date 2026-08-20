package com.auditplatform.audit.api;

import java.time.LocalDate;

public record UpdateCapaRequest(
        String description,
        LocalDate dueOn,
        String notes
) {
}
