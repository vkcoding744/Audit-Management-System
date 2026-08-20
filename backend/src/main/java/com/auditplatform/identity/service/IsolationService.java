package com.auditplatform.identity.service;

import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.common.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class IsolationService {

    public PlatformPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof PlatformPrincipal principal)) {
            throw new ApiException(ErrorCode.SYS_UNAUTHORIZED, "Authentication required");
        }
        return principal;
    }

    public void assertCanAccessTenant(String resourceTenantId) {
        PlatformPrincipal principal = requirePrincipal();
        if (principal.platformAdmin()) {
            return;
        }
        if (resourceTenantId == null || principal.tenantId() == null || !principal.tenantId().equals(resourceTenantId)) {
            throw new ApiException(ErrorCode.AUTH_TENANT_MISMATCH, "Cross-tenant access is not allowed");
        }
    }

    public String requireTenantScope() {
        String tenantId = effectiveTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Tenant scope is required (send X-Tenant-Id for platform administrators)");
        }
        return tenantId;
    }

    public String effectiveTenantId() {
        PlatformPrincipal principal = requirePrincipal();
        if (principal.platformAdmin()) {
            return TenantContext.getTenantId();
        }
        return principal.tenantId();
    }
}
