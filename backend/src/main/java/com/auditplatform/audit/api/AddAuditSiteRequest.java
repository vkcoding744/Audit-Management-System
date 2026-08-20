package com.auditplatform.audit.api;

import jakarta.validation.constraints.NotBlank;

public record AddAuditSiteRequest(@NotBlank String siteId) {
}
