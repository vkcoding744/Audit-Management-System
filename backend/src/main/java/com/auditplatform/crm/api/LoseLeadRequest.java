package com.auditplatform.crm.api;

import jakarta.validation.constraints.NotBlank;

public record LoseLeadRequest(@NotBlank String reason) {
}
