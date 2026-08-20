package com.auditplatform.audit.service;

import com.auditplatform.audit.domain.AuditProgramme;
import com.auditplatform.audit.repository.AuditProgrammeRepository;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.repository.SchemeStandardRepository;
import com.auditplatform.standards.service.SchemeService;
import com.auditplatform.standards.service.StandardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProgrammeServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsProgramme() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("AUDIT_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        AuditProgramme foreign = new AuditProgramme();
        foreign.setTenantId("tenant-b");
        AuditProgrammeRepository programmes = mock(AuditProgrammeRepository.class);
        when(programmes.findByIdAndDeletedAtIsNull("p1")).thenReturn(Optional.of(foreign));

        ProgrammeService service = new ProgrammeService(
                programmes,
                mock(AuditRepository.class),
                mock(AuditNumberService.class),
                mock(ClientService.class),
                mock(SchemeService.class),
                mock(StandardService.class),
                mock(SchemeStandardRepository.class),
                isolationService,
                mock(AuditLogService.class)
        );

        assertThatThrownBy(() -> service.get("p1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
