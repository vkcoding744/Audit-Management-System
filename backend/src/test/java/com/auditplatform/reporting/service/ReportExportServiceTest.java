package com.auditplatform.reporting.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.document.storage.ObjectStoragePort;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.reporting.domain.ReportDataset;
import com.auditplatform.reporting.domain.ReportDefinition;
import com.auditplatform.reporting.domain.ReportDefinitionStatus;
import com.auditplatform.reporting.domain.ReportFormat;
import com.auditplatform.reporting.repository.ReportExportRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportExportServiceTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-21T12:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void runBlockedWhenDefinitionIsArchived() {
        bindUser();
        ReportDefinition definition = archived();
        ReportDefinitionService definitions = mock(ReportDefinitionService.class);
        when(definitions.requireDefinition("rpt-1")).thenReturn(definition);

        ReportExportService service = new ReportExportService(
                mock(ReportExportRepository.class),
                definitions,
                mock(ReportNumberService.class),
                mock(ReportDatasetQueryService.class),
                mock(ReportRenderer.class),
                mock(ObjectStoragePort.class),
                isolationService,
                mock(AuditLogService.class),
                clock
        );

        assertThatThrownBy(() -> service.run("rpt-1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("Archived reports cannot be run");
                });
    }

    private static ReportDefinition archived() {
        ReportDefinition definition = new ReportDefinition();
        ReflectionTestUtils.setField(definition, "id", "rpt-1");
        definition.setTenantId("tenant-a");
        definition.setReportNumber("RPT-000001");
        definition.setName("Clients");
        definition.setDataset(ReportDataset.CLIENTS);
        definition.setFormat(ReportFormat.CSV);
        definition.setStatus(ReportDefinitionStatus.ARCHIVED);
        return definition;
    }

    private static void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("REPORT_EXPORT")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
