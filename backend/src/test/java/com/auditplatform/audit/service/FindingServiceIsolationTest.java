package com.auditplatform.audit.service;

import com.auditplatform.audit.domain.Finding;
import com.auditplatform.audit.repository.CapaActionRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.service.SiteService;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindingServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsFinding() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("AUDIT_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        Finding foreign = new Finding();
        foreign.setTenantId("tenant-b");
        FindingRepository findings = mock(FindingRepository.class);
        when(findings.findByIdAndDeletedAtIsNull("f1")).thenReturn(Optional.of(foreign));

        FindingService service = new FindingService(
                findings,
                mock(CapaActionRepository.class),
                mock(AuditNumberService.class),
                mock(AuditService.class),
                mock(ExecutionService.class),
                mock(SiteService.class),
                isolationService,
                mock(AuditLogService.class),
                Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> service.get("f1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
