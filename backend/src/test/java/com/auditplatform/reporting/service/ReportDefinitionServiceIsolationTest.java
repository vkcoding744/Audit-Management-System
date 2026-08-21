package com.auditplatform.reporting.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.reporting.domain.ReportDefinition;
import com.auditplatform.reporting.repository.ReportDefinitionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportDefinitionServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsReport() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("REPORT_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        ReportDefinition foreign = new ReportDefinition();
        foreign.setTenantId("tenant-b");
        ReportDefinitionRepository definitions = mock(ReportDefinitionRepository.class);
        when(definitions.findByIdAndDeletedAtIsNull("rpt-1")).thenReturn(Optional.of(foreign));

        ReportDefinitionService service = new ReportDefinitionService(
                definitions,
                mock(ReportNumberService.class),
                isolationService,
                mock(AuditLogService.class)
        );

        assertThatThrownBy(() -> service.get("rpt-1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
