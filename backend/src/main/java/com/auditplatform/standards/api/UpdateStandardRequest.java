package com.auditplatform.standards.api;

import com.auditplatform.standards.domain.StandardStatus;
import jakarta.validation.constraints.Size;

public record UpdateStandardRequest(
        @Size(max = 64) String code,
        @Size(max = 255) String name,
        @Size(max = 255) String publisher,
        @Size(max = 64) String edition,
        String description,
        StandardStatus status,
        String notes
) {
}
