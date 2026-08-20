package com.auditplatform.training.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.service.AuditorService;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.service.SchemeService;
import com.auditplatform.standards.service.StandardService;
import com.auditplatform.training.domain.TrainingRecord;
import com.auditplatform.training.domain.TrainingStatus;
import com.auditplatform.training.repository.TrainingRecordRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrainingRecordServiceTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-20T12:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void expiredFlagIsTrueWhenCompletedTrainingIsPastExpiry() {
        bindUser();
        TrainingRecord record = new TrainingRecord();
        ReflectionTestUtils.setField(record, "id", "trn-1");
        record.setTenantId("tenant-a");
        record.setTrainingNumber("TRN-000001");
        record.setAuditorId("aud-1");
        record.setTitle("Lead auditor course");
        record.setStatus(TrainingStatus.COMPLETED);
        record.setCompletedOn(LocalDate.of(2025, 1, 1));
        record.setExpiresOn(LocalDate.of(2026, 8, 1));

        TrainingRecordRepository records = mock(TrainingRecordRepository.class);
        when(records.findByIdAndDeletedAtIsNull("trn-1")).thenReturn(Optional.of(record));

        TrainingRecordService service = new TrainingRecordService(
                records,
                mock(TrainingNumberService.class),
                mock(AuditorService.class),
                mock(StandardService.class),
                mock(SchemeService.class),
                isolationService,
                mock(AuditLogService.class),
                clock
        );

        assertThat(service.get("trn-1").expired()).isTrue();
    }

    private static void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("TRAINING_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
