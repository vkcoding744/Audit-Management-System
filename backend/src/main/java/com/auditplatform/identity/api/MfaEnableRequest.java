package com.auditplatform.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaEnableRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "MFA code must be 6 digits")
        String code
) {
}
