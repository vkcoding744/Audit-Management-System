package com.auditplatform.training.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.service.AuditorService;
import com.auditplatform.auditor.service.CompetencyService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.service.SchemeService;
import com.auditplatform.standards.service.StandardService;
import com.auditplatform.training.api.CompleteAssessmentRequest;
import com.auditplatform.training.domain.AssessmentResult;
import com.auditplatform.training.domain.AssessmentStatus;
import com.auditplatform.training.domain.CompetencyAssessment;
import com.auditplatform.training.repository.CompetencyAssessmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompetencyAssessmentServiceTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void completeBlockedWhenAlreadyRecorded() {
        bindUser();
        CompetencyAssessment assessment = recorded();
        CompetencyAssessmentRepository assessments = mock(CompetencyAssessmentRepository.class);
        when(assessments.findByIdAndDeletedAtIsNull("asm-1")).thenReturn(Optional.of(assessment));
        CompetencyAssessmentService service = service(assessments);

        assertThatThrownBy(() -> service.complete("asm-1", new CompleteAssessmentRequest(AssessmentResult.PASS, null)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("Recorded assessments cannot be changed");
                });
    }

    private CompetencyAssessmentService service(CompetencyAssessmentRepository assessments) {
        return new CompetencyAssessmentService(
                assessments,
                mock(TrainingNumberService.class),
                mock(AuditorService.class),
                mock(CompetencyService.class),
                mock(StandardService.class),
                mock(SchemeService.class),
                isolationService,
                mock(AuditLogService.class)
        );
    }

    private static CompetencyAssessment recorded() {
        CompetencyAssessment assessment = new CompetencyAssessment();
        ReflectionTestUtils.setField(assessment, "id", "asm-1");
        assessment.setTenantId("tenant-a");
        assessment.setAssessmentNumber("ASM-000001");
        assessment.setAuditorId("aud-1");
        assessment.setAssessedOn(LocalDate.of(2026, 8, 1));
        assessment.setStatus(AssessmentStatus.RECORDED);
        assessment.setResult(AssessmentResult.PASS);
        return assessment;
    }

    private static void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("TRAINING_UPDATE")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
