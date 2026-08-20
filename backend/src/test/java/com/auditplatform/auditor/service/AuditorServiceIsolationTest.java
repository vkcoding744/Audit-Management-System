package com.auditplatform.auditor.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.repository.AuditorQualificationRepository;
import com.auditplatform.auditor.repository.AuditorRepository;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.repository.UserAccountRepository;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditorServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsAuditor() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("AUDITOR_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        Auditor foreign = new Auditor();
        foreign.setTenantId("tenant-b");
        AuditorRepository auditors = mock(AuditorRepository.class);
        when(auditors.findByIdAndDeletedAtIsNull("a1")).thenReturn(Optional.of(foreign));

        AuditorService service = new AuditorService(
                auditors,
                mock(AuditorQualificationRepository.class),
                mock(AuditorNumberService.class),
                mock(UserAccountRepository.class),
                isolationService,
                mock(AuditLogService.class)
        );

        assertThatThrownBy(() -> service.get("a1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
