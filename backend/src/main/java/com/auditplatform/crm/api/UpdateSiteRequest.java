package com.auditplatform.crm.api;

import com.auditplatform.crm.domain.SiteStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateSiteRequest(
        @Size(max = 255) String name,
        @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @Size(max = 128) String city,
        @Size(max = 128) String state,
        @Size(max = 32) String postalCode,
        @Size(max = 128) String country,
        String scope,
        @Min(0) Integer employeeCount,
        String processes,
        SiteStatus status
) {
}
