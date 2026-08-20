package com.auditplatform.crm.api;

import com.auditplatform.crm.domain.Site;
import com.auditplatform.crm.domain.SiteStatus;

public record SiteResponse(
        String id,
        String tenantId,
        String clientId,
        String name,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        String scope,
        Integer employeeCount,
        String processes,
        SiteStatus status
) {
    public static SiteResponse from(Site site) {
        return new SiteResponse(
                site.getId(),
                site.getTenantId(),
                site.getClientId(),
                site.getName(),
                site.getAddressLine1(),
                site.getAddressLine2(),
                site.getCity(),
                site.getState(),
                site.getPostalCode(),
                site.getCountry(),
                site.getScope(),
                site.getEmployeeCount(),
                site.getProcesses(),
                site.getStatus()
        );
    }
}
