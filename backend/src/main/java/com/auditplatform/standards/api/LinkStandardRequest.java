package com.auditplatform.standards.api;

import jakarta.validation.constraints.NotBlank;

public record LinkStandardRequest(@NotBlank String standardId) {
}
