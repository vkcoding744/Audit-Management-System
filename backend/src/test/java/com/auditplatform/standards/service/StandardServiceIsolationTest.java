package com.auditplatform.standards.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.api.UpdateStandardRequest;
import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.domain.StandardStatus;
import com.auditplatform.standards.repository.ChecklistRepository;
import com.auditplatform.standards.repository.StandardClauseRepository;
import com.auditplatform.standards.repository.StandardRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StandardServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsStandard() {
        authenticate("tenant-a");
        Standard foreign = new Standard();
        foreign.setTenantId("tenant-b");
        foreign.setCode("STD-B");
        StandardRepository standards = mock(StandardRepository.class);
        when(standards.findByIdAndDeletedAtIsNull("s1")).thenReturn(Optional.of(foreign));

        StandardService service = new StandardService(
                standards,
                mock(StandardClauseRepository.class),
                mock(ChecklistRepository.class),
                isolationService,
                mock(AuditLogService.class)
        );

        assertThatThrownBy(() -> service.get("s1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }

    @Test
    void publishedStandardCannotBePatched() {
        authenticate("tenant-a");
        Standard published = new Standard();
        published.setTenantId("tenant-a");
        published.setCode("STD-A");
        published.setStatus(StandardStatus.PUBLISHED);
        StandardRepository standards = mock(StandardRepository.class);
        when(standards.findByIdAndDeletedAtIsNull("s1")).thenReturn(Optional.of(published));

        StandardService service = new StandardService(
                standards,
                mock(StandardClauseRepository.class),
                mock(ChecklistRepository.class),
                isolationService,
                mock(AuditLogService.class)
        );

        assertThatThrownBy(() -> service.update("s1", new UpdateStandardRequest(null, "New name", null, null, null, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SYS_VALIDATION);
    }

    private void authenticate(String tenantId) {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", tenantId, false, "sid", Set.of("STANDARD_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
