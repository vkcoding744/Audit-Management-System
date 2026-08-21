package com.auditplatform.dashboard.service;

import com.auditplatform.ai.repository.AiGenerationRepository;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.CapaActionRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.repository.ClientRepository;
import com.auditplatform.finance.repository.InvoiceRepository;
import com.auditplatform.governance.repository.AppealRepository;
import com.auditplatform.governance.repository.ComplaintRepository;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantDashboardServiceTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-21T12:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void summaryRequiresTenantScope() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", null, true, "sid", Set.of("DASHBOARD_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertThatThrownBy(() -> service(
                mock(ClientRepository.class),
                mock(AuditRepository.class),
                mock(FindingRepository.class),
                mock(CapaActionRepository.class),
                mock(CertificateRepository.class),
                mock(InvoiceRepository.class),
                mock(ComplaintRepository.class),
                mock(AppealRepository.class),
                mock(AiGenerationRepository.class)
        ).summary())
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SYS_VALIDATION);
    }

    @Test
    void summaryUsesAuthenticatedTenantCounts() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("DASHBOARD_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        ClientRepository clients = mock(ClientRepository.class);
        when(clients.countByTenantIdAndDeletedAtIsNull("tenant-a")).thenReturn(4L);
        FindingRepository findings = mock(FindingRepository.class);
        when(findings.countByTenantIdAndStatusAndDeletedAtIsNull(eq("tenant-a"), any())).thenReturn(2L);

        var summary = service(
                clients,
                mock(AuditRepository.class),
                findings,
                mock(CapaActionRepository.class),
                mock(CertificateRepository.class),
                mock(InvoiceRepository.class),
                mock(ComplaintRepository.class),
                mock(AppealRepository.class),
                mock(AiGenerationRepository.class)
        ).summary();

        assertThat(summary.clients()).isEqualTo(4);
        assertThat(summary.openFindings()).isEqualTo(2);
    }

    private TenantDashboardService service(
            ClientRepository clients,
            AuditRepository audits,
            FindingRepository findings,
            CapaActionRepository capas,
            CertificateRepository certificates,
            InvoiceRepository invoices,
            ComplaintRepository complaints,
            AppealRepository appeals,
            AiGenerationRepository ai
    ) {
        return new TenantDashboardService(
                isolationService,
                clients,
                audits,
                findings,
                capas,
                certificates,
                invoices,
                complaints,
                appeals,
                ai,
                clock
        );
    }
}
