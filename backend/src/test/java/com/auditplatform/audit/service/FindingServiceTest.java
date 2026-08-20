package com.auditplatform.audit.service;

import com.auditplatform.audit.api.UpdateFindingRequest;
import com.auditplatform.audit.domain.CapaStatus;
import com.auditplatform.audit.domain.Finding;
import com.auditplatform.audit.domain.FindingSeverity;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.audit.repository.CapaActionRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.service.SiteService;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindingServiceTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void closedFindingCannotBeEdited() {
        bindUser();
        Finding closed = openFinding();
        closed.setStatus(FindingStatus.CLOSED);
        FindingRepository findings = mock(FindingRepository.class);
        when(findings.findByIdAndDeletedAtIsNull("f1")).thenReturn(Optional.of(closed));

        FindingService service = service(findings, mock(CapaActionRepository.class));

        assertThatThrownBy(() -> service.update("f1", new UpdateFindingRequest("New title", null, null, null)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("Closed findings cannot be edited");
                });
    }

    @Test
    void majorFindingCannotCloseWithoutCapa() {
        bindUser();
        Finding finding = openFinding();
        finding.setSeverity(FindingSeverity.MAJOR);
        FindingRepository findings = mock(FindingRepository.class);
        when(findings.findByIdAndDeletedAtIsNull("f1")).thenReturn(Optional.of(finding));
        CapaActionRepository capas = mock(CapaActionRepository.class);
        when(capas.countByFindingIdAndDeletedAtIsNull("f1")).thenReturn(0L);

        FindingService service = service(findings, capas);

        assertThatThrownBy(() -> service.close("f1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SYS_VALIDATION);
    }

    @Test
    void majorFindingCannotCloseWithOpenCapa() {
        bindUser();
        Finding finding = openFinding();
        finding.setSeverity(FindingSeverity.MAJOR);
        FindingRepository findings = mock(FindingRepository.class);
        when(findings.findByIdAndDeletedAtIsNull("f1")).thenReturn(Optional.of(finding));
        CapaActionRepository capas = mock(CapaActionRepository.class);
        when(capas.countByFindingIdAndDeletedAtIsNull("f1")).thenReturn(1L);
        when(capas.existsByFindingIdAndStatusAndDeletedAtIsNull("f1", CapaStatus.OPEN)).thenReturn(true);

        FindingService service = service(findings, capas);

        assertThatThrownBy(() -> service.close("f1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SYS_VALIDATION);
    }

    private FindingService service(FindingRepository findings, CapaActionRepository capas) {
        when(capas.findByTenantIdAndFindingIdAndDeletedAtIsNullOrderByDueOnAsc("tenant-a", "f1")).thenReturn(List.of());
        return new FindingService(
                findings,
                capas,
                mock(AuditNumberService.class),
                mock(AuditService.class),
                mock(ExecutionService.class),
                mock(SiteService.class),
                isolationService,
                mock(AuditLogService.class),
                clock
        );
    }

    private static Finding openFinding() {
        Finding finding = new Finding();
        ReflectionTestUtils.setField(finding, "id", "f1");
        finding.setTenantId("tenant-a");
        finding.setStatus(FindingStatus.OPEN);
        finding.setSeverity(FindingSeverity.MINOR);
        return finding;
    }

    private static void bindUser() {
        var principal = new com.auditplatform.common.security.PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("FINDING_UPDATE")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
