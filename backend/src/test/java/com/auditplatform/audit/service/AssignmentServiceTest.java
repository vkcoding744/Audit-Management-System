package com.auditplatform.audit.service;

import com.auditplatform.audit.api.CreateAssignmentRequest;
import com.auditplatform.audit.domain.AssignmentRole;
import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.repository.AuditAssignmentRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.api.EligibilityResponse;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.service.AuditorEligibilityService;
import com.auditplatform.auditor.service.AuditorService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignmentServiceTest {

    @Test
    void expiredCompetencyBlocksAssignment() {
        Audit audit = new Audit();
        audit.setTenantId("tenant-a");
        audit.setSchemeId("scheme-1");
        audit.setStandardId("std-1");
        audit.setPlannedStartOn(LocalDate.of(2026, 8, 20));

        Auditor auditor = new Auditor();
        auditor.setTenantId("tenant-a");

        AuditService auditService = mock(AuditService.class);
        when(auditService.requirePlannable("audit-1")).thenReturn(audit);

        AuditorService auditorService = mock(AuditorService.class);
        when(auditorService.requireAuditor("auditor-1")).thenReturn(auditor);

        AuditorEligibilityService eligibilityService = mock(AuditorEligibilityService.class);
        when(eligibilityService.evaluate(eq("auditor-1"), eq("std-1"), eq("scheme-1"), eq(LocalDate.of(2026, 8, 20))))
                .thenReturn(new EligibilityResponse(
                        "auditor-1",
                        "std-1",
                        "scheme-1",
                        LocalDate.of(2026, 8, 20),
                        false,
                        List.of("COMPETENCY_EXPIRED")
                ));

        AuditAssignmentRepository assignments = mock(AuditAssignmentRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC);
        AssignmentService service = new AssignmentService(
                assignments,
                auditService,
                auditorService,
                eligibilityService,
                mock(IsolationService.class),
                mock(AuditLogService.class),
                clock
        );

        assertThatThrownBy(() -> service.assign("audit-1", new CreateAssignmentRequest("auditor-1", AssignmentRole.LEAD)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("COMPETENCY_EXPIRED");
                });

        verify(eligibilityService).evaluate("auditor-1", "std-1", "scheme-1", LocalDate.of(2026, 8, 20));
    }

    @Test
    void eligibleAuditorCanBeAssignedWhenNoExistingLead() {
        Audit audit = new Audit();
        audit.setTenantId("tenant-a");
        audit.setSchemeId("scheme-1");
        audit.setStandardId("std-1");
        audit.setPlannedStartOn(LocalDate.of(2026, 8, 20));

        Auditor auditor = new Auditor();
        auditor.setTenantId("tenant-a");

        AuditService auditService = mock(AuditService.class);
        when(auditService.requirePlannable("audit-1")).thenReturn(audit);

        AuditorService auditorService = mock(AuditorService.class);
        when(auditorService.requireAuditor("auditor-1")).thenReturn(auditor);

        AuditorEligibilityService eligibilityService = mock(AuditorEligibilityService.class);
        when(eligibilityService.evaluate(eq("auditor-1"), eq("std-1"), eq("scheme-1"), eq(LocalDate.of(2026, 8, 20))))
                .thenReturn(new EligibilityResponse(
                        "auditor-1",
                        "std-1",
                        "scheme-1",
                        LocalDate.of(2026, 8, 20),
                        true,
                        List.of()
                ));

        AuditAssignmentRepository assignments = mock(AuditAssignmentRepository.class);
        when(assignments.existsByAuditIdAndAuditorIdAndDeletedAtIsNull(isNull(), isNull())).thenReturn(false);
        when(assignments.existsByAuditIdAndAssignmentRoleAndDeletedAtIsNull(isNull(), eq(AssignmentRole.LEAD)))
                .thenReturn(false);
        when(assignments.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        Clock clock = Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC);
        AssignmentService service = new AssignmentService(
                assignments,
                auditService,
                auditorService,
                eligibilityService,
                mock(IsolationService.class),
                mock(AuditLogService.class),
                clock
        );

        var result = service.assign("audit-1", new CreateAssignmentRequest("auditor-1", AssignmentRole.LEAD));
        assertThat(result.assignmentRole()).isEqualTo(AssignmentRole.LEAD);
        assertThat(result.auditorId()).isEqualTo("auditor-1");
    }
}
