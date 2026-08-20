package com.auditplatform.training.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.service.AuditorService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.service.SchemeService;
import com.auditplatform.standards.service.StandardService;
import com.auditplatform.training.domain.TrainingRecord;
import com.auditplatform.training.repository.TrainingRecordRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrainingRecordServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsTrainingRecord() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("TRAINING_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        TrainingRecord foreign = new TrainingRecord();
        foreign.setTenantId("tenant-b");
        TrainingRecordRepository records = mock(TrainingRecordRepository.class);
        when(records.findByIdAndDeletedAtIsNull("trn-1")).thenReturn(Optional.of(foreign));

        TrainingRecordService service = new TrainingRecordService(
                records,
                mock(TrainingNumberService.class),
                mock(AuditorService.class),
                mock(StandardService.class),
                mock(SchemeService.class),
                isolationService,
                mock(AuditLogService.class),
                Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> service.get("trn-1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
