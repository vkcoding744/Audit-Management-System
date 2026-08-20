package com.auditplatform.audit.service;

import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.repository.AuditAssignmentRepository;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.AuditSiteRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.service.SiteService;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.service.ChecklistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsAudit() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("AUDIT_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        Audit foreign = new Audit();
        foreign.setTenantId("tenant-b");
        AuditRepository audits = mock(AuditRepository.class);
        when(audits.findByIdAndDeletedAtIsNull("a1")).thenReturn(Optional.of(foreign));

        AuditService service = new AuditService(
                audits,
                mock(AuditSiteRepository.class),
                mock(AuditAssignmentRepository.class),
                mock(AuditNumberService.class),
                mock(ProgrammeService.class),
                mock(SiteService.class),
                mock(ChecklistService.class),
                isolationService,
                mock(AuditLogService.class)
        );

        assertThatThrownBy(() -> service.get("a1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
