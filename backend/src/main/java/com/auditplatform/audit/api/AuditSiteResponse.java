package com.auditplatform.audit.api;

import com.auditplatform.audit.domain.AuditSite;

public record AuditSiteResponse(
        String id,
        String tenantId,
        String auditId,
        String siteId
) {
    public static AuditSiteResponse from(AuditSite site) {
        return new AuditSiteResponse(site.getId(), site.getTenantId(), site.getAuditId(), site.getSiteId());
    }
}
