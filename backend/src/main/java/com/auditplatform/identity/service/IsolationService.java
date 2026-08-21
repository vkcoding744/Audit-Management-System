package com.auditplatform.identity.service;

import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.persistence.TenantAwareEntity;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.common.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.hibernate.Session;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class IsolationService {

    private final ObjectProvider<EntityManager> entityManagers;

    public IsolationService() {
        this.entityManagers = null;
    }

    @Autowired
    public IsolationService(ObjectProvider<EntityManager> entityManagers) {
        this.entityManagers = entityManagers;
    }

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
        applyTenantFilter(tenantId);
        return tenantId;
    }

    public String effectiveTenantId() {
        PlatformPrincipal principal = requirePrincipal();
        if (principal.platformAdmin()) {
            String headerTenant = TenantContext.getTenantId();
            applyTenantFilter(headerTenant);
            return headerTenant;
        }
        applyTenantFilter(principal.tenantId());
        return principal.tenantId();
    }

    /**
     * Enables Hibernate {@code tenantIsolation} when an EntityManager is joined to a transaction.
     * No-ops in unit tests that construct this service without JPA.
     */
    void applyTenantFilter(String tenantId) {
        if (entityManagers == null) {
            return;
        }
        EntityManager entityManager = entityManagers.getIfAvailable();
        if (entityManager == null) {
            return;
        }
        try {
            if (!entityManager.isOpen() || !entityManager.isJoinedToTransaction()) {
                return;
            }
            Session session = entityManager.unwrap(Session.class);
            if (tenantId == null || tenantId.isBlank()) {
                session.disableFilter(TenantAwareEntity.TENANT_FILTER);
                return;
            }
            session.enableFilter(TenantAwareEntity.TENANT_FILTER).setParameter("tenantId", tenantId);
        } catch (IllegalStateException | PersistenceException ignored) {
            // No Hibernate session (WebMvc tests, non-JPA threads)
        }
    }
}
