package com.auditplatform.governance.service;

import com.auditplatform.audit.service.FindingService;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.certification.service.CertificateService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.governance.api.CloseComplaintRequest;
import com.auditplatform.governance.api.DecideAppealRequest;
import com.auditplatform.governance.domain.Appeal;
import com.auditplatform.governance.domain.AppealOutcome;
import com.auditplatform.governance.domain.AppealStatus;
import com.auditplatform.governance.domain.Complaint;
import com.auditplatform.governance.domain.ComplaintStatus;
import com.auditplatform.governance.repository.AppealRepository;
import com.auditplatform.governance.repository.ComplaintRepository;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GovernanceWorkflowTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-20T12:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void closeBlockedWhenComplaintAlreadyClosed() {
        bindUser();
        Complaint complaint = new Complaint();
        ReflectionTestUtils.setField(complaint, "id", "cmp-1");
        complaint.setTenantId("tenant-a");
        complaint.setComplaintNumber("CMP-000001");
        complaint.setSubject("Delay");
        complaint.setStatus(ComplaintStatus.CLOSED);
        ComplaintRepository complaints = mock(ComplaintRepository.class);
        when(complaints.findByIdAndDeletedAtIsNull("cmp-1")).thenReturn(Optional.of(complaint));
        ComplaintService service = new ComplaintService(
                complaints,
                mock(GovernanceNumberService.class),
                mock(ClientService.class),
                isolationService,
                mock(AuditLogService.class),
                clock
        );

        assertThatThrownBy(() -> service.close("cmp-1", new CloseComplaintRequest("Done")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("Closed complaints cannot be changed");
                });
    }

    @Test
    void decideBlockedWhenAppealAlreadyDecided() {
        bindUser();
        Appeal appeal = new Appeal();
        ReflectionTestUtils.setField(appeal, "id", "apl-1");
        appeal.setTenantId("tenant-a");
        appeal.setAppealNumber("APL-000001");
        appeal.setSubject("Scope");
        appeal.setReceivedOn(LocalDate.of(2026, 8, 1));
        appeal.setStatus(AppealStatus.DISMISSED);
        appeal.setOutcome(AppealOutcome.DISMISSED);
        AppealRepository appeals = mock(AppealRepository.class);
        when(appeals.findByIdAndDeletedAtIsNull("apl-1")).thenReturn(Optional.of(appeal));
        AppealService service = new AppealService(
                appeals,
                mock(GovernanceNumberService.class),
                mock(ClientService.class),
                mock(CertificateService.class),
                mock(FindingService.class),
                isolationService,
                mock(AuditLogService.class),
                clock
        );

        assertThatThrownBy(() -> service.decide("apl-1", new DecideAppealRequest(AppealOutcome.UPHELD, null)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("Decided appeals cannot be changed");
                });
    }

    private static void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("COMPLAINT_UPDATE", "APPEAL_UPDATE")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
