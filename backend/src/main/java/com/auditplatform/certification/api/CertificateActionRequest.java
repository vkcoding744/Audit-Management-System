package com.auditplatform.certification.api;

import jakarta.validation.constraints.NotBlank;

public record CertificateActionRequest(@NotBlank String reason) {
}
