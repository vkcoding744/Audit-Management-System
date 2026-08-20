package com.auditplatform.governance.api;

import jakarta.validation.constraints.NotBlank;

public record CloseComplaintRequest(
        @NotBlank String resolution
) {
}
