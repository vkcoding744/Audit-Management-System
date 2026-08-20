package com.auditplatform.audit.service;

import com.auditplatform.audit.domain.AuditChecklistResponse;
import com.auditplatform.audit.repository.AuditChecklistResponseRepository;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.service.ChecklistService;
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

class ExecutionServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsResponse() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("AUDIT_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        AuditChecklistResponse foreign = new AuditChecklistResponse();
        foreign.setTenantId("tenant-b");
        AuditChecklistResponseRepository responses = mock(AuditChecklistResponseRepository.class);
        when(responses.findByIdAndDeletedAtIsNull("r1")).thenReturn(Optional.of(foreign));

        ExecutionService service = new ExecutionService(
                mock(AuditService.class),
                mock(AuditRepository.class),
                responses,
                mock(ChecklistService.class),
                isolationService,
                mock(AuditLogService.class),
                Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> service.getResponse("r1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
