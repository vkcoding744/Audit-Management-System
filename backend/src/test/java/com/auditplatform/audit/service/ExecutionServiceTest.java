package com.auditplatform.audit.service;

import com.auditplatform.audit.api.AuditResponse;
import com.auditplatform.audit.api.UpdateAuditItemRequest;
import com.auditplatform.audit.domain.AssessmentResult;
import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditChecklistResponse;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.repository.AuditChecklistResponseRepository;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.domain.Checklist;
import com.auditplatform.standards.domain.ChecklistItem;
import com.auditplatform.standards.domain.ChecklistStatus;
import com.auditplatform.standards.service.ChecklistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExecutionServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void completeBlockedWhenRequiredItemUnanswered() {
        Audit audit = inProgressAudit();
        AuditService auditService = mock(AuditService.class);
        when(auditService.requireAudit("a1")).thenReturn(audit);
        AuditChecklistResponseRepository responses = mock(AuditChecklistResponseRepository.class);
        when(responses.countByAuditIdAndRequiredIsTrueAndResultAndDeletedAtIsNull("a1", AssessmentResult.NOT_ASSESSED))
                .thenReturn(2L);

        ExecutionService service = service(auditService, mock(AuditRepository.class), responses, mock(ChecklistService.class));

        assertThatThrownBy(() -> service.complete("a1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("required checklist item");
                });
    }

    @Test
    void startSnapshotsActiveChecklistItems() {
        Audit audit = scheduledAudit();
        AuditService auditService = mock(AuditService.class);
        when(auditService.requireAudit("a1")).thenReturn(audit);
        when(auditService.get("a1")).thenReturn(AuditResponse.summary(audit));

        Checklist checklist = new Checklist();
        checklist.setStatus(ChecklistStatus.ACTIVE);
        ChecklistItem item = new ChecklistItem();
        item.setTitle("Documented information");
        item.setRequired(true);
        ReflectionTestUtils.setField(item, "id", "item-1");

        ChecklistService checklists = mock(ChecklistService.class);
        when(checklists.requireChecklist("cl-1")).thenReturn(checklist);
        when(checklists.listItems("cl-1")).thenReturn(List.of(item));

        AuditChecklistResponseRepository responses = mock(AuditChecklistResponseRepository.class);
        when(responses.existsByAuditIdAndDeletedAtIsNull("a1")).thenReturn(false);
        when(responses.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuditRepository audits = mock(AuditRepository.class);
        ExecutionService service = service(auditService, audits, responses, checklists);

        service.start("a1");

        assertThat(audit.getStatus()).isEqualTo(AuditStatus.IN_PROGRESS);
        verify(responses).save(any(AuditChecklistResponse.class));
        verify(audits).save(audit);
    }

    @Test
    void nonconformityWithoutCommentIsRejected() {
        bindUser();
        Audit audit = inProgressAudit();
        AuditChecklistResponse response = new AuditChecklistResponse();
        response.setTenantId("tenant-a");
        response.setAuditId("a1");
        response.setResult(AssessmentResult.NOT_ASSESSED);

        AuditService auditService = mock(AuditService.class);
        when(auditService.requireAudit("a1")).thenReturn(audit);
        AuditChecklistResponseRepository responses = mock(AuditChecklistResponseRepository.class);
        when(responses.findByIdAndDeletedAtIsNull("r1")).thenReturn(java.util.Optional.of(response));

        ExecutionService service = service(auditService, mock(AuditRepository.class), responses, mock(ChecklistService.class));

        assertThatThrownBy(() -> service.updateResponse("r1", new UpdateAuditItemRequest(AssessmentResult.NONCONFORMING, "  ")))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SYS_VALIDATION);
    }

    private ExecutionService service(
            AuditService auditService,
            AuditRepository audits,
            AuditChecklistResponseRepository responses,
            ChecklistService checklists
    ) {
        return new ExecutionService(
                auditService,
                audits,
                responses,
                checklists,
                new IsolationService(),
                mock(AuditLogService.class),
                clock
        );
    }

    private static Audit scheduledAudit() {
        Audit audit = new Audit();
        ReflectionTestUtils.setField(audit, "id", "a1");
        audit.setTenantId("tenant-a");
        audit.setStatus(AuditStatus.SCHEDULED);
        audit.setChecklistId("cl-1");
        return audit;
    }

    private static Audit inProgressAudit() {
        Audit audit = new Audit();
        ReflectionTestUtils.setField(audit, "id", "a1");
        audit.setTenantId("tenant-a");
        audit.setStatus(AuditStatus.IN_PROGRESS);
        return audit;
    }

    private static void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("AUDIT_UPDATE")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
