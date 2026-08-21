package com.auditplatform.notification.service;

import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.notification.api.NotificationJobResponse;
import com.auditplatform.notification.domain.NotificationChannelType;
import com.auditplatform.notification.domain.NotificationJob;
import com.auditplatform.notification.domain.NotificationJobStatus;
import com.auditplatform.notification.repository.NotificationJobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationDispatchServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-21T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void dispatchSendsDueQueuedJobWithoutPrincipal() {
        NotificationJob job = queued("job-due", "tenant-a");
        NotificationJobRepository jobs = mock(NotificationJobRepository.class);
        NotificationJobService jobService = mock(NotificationJobService.class);
        when(jobs.findDueQueued(eq(clock.instant()), isNull(), any(Pageable.class))).thenReturn(List.of(job));
        when(jobService.deliverQueuedJob("job-due")).thenReturn(sentResponse(job));

        var result = service(jobs, jobService).dispatchDueJobs(null, 50);

        assertThat(result.sent()).isEqualTo(1);
        assertThat(result.failed()).isZero();
        assertThat(result.considered()).isEqualTo(1);
        verify(jobService).deliverQueuedJob("job-due");
    }

    @Test
    void dispatchSkipsWhenRepositoryReturnsNoDueJobs() {
        NotificationJobRepository jobs = mock(NotificationJobRepository.class);
        NotificationJobService jobService = mock(NotificationJobService.class);
        when(jobs.findDueQueued(eq(clock.instant()), eq("tenant-a"), any(Pageable.class))).thenReturn(List.of());

        var result = service(jobs, jobService).dispatchDueJobs("tenant-a", 50);

        assertThat(result.considered()).isZero();
        verify(jobService, never()).deliverQueuedJob(any());
    }

    @Test
    void alreadySentJobIsNotResent() {
        NotificationJob job = queued("job-sent", "tenant-a");
        NotificationJobRepository jobs = mock(NotificationJobRepository.class);
        NotificationJobService jobService = mock(NotificationJobService.class);
        when(jobs.findDueQueued(eq(clock.instant()), eq("tenant-a"), any(Pageable.class))).thenReturn(List.of(job));
        when(jobService.deliverQueuedJob("job-sent"))
                .thenThrow(new ApiException(ErrorCode.SYS_VALIDATION, "Only queued jobs can be sent"));

        var result = service(jobs, jobService).dispatchDueJobs("tenant-a", 50);

        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.sent()).isZero();
    }

    @Test
    void tenantScopedDispatchOnlyLoadsThatTenant() {
        NotificationJob jobA = queued("job-a", "tenant-a");
        NotificationJobRepository jobs = mock(NotificationJobRepository.class);
        NotificationJobService jobService = mock(NotificationJobService.class);
        when(jobs.findDueQueued(eq(clock.instant()), eq("tenant-a"), any(Pageable.class))).thenReturn(List.of(jobA));
        when(jobService.deliverQueuedJob("job-a")).thenReturn(sentResponse(jobA));

        var result = service(jobs, jobService).dispatchDueJobs("tenant-a", 50);

        assertThat(result.sent()).isEqualTo(1);
        verify(jobs).findDueQueued(eq(clock.instant()), eq("tenant-a"), any(Pageable.class));
        verify(jobs, never()).findDueQueued(eq(clock.instant()), eq("tenant-b"), any(Pageable.class));
        verify(jobService, never()).deliverQueuedJob("job-b");
    }

    private NotificationDispatchService service(NotificationJobRepository jobs, NotificationJobService jobService) {
        return new NotificationDispatchService(
                jobs,
                jobService,
                mock(IsolationService.class),
                clock,
                50
        );
    }

    private static NotificationJob queued(String id, String tenantId) {
        NotificationJob job = new NotificationJob();
        ReflectionTestUtils.setField(job, "id", id);
        job.setTenantId(tenantId);
        job.setJobNumber("NTF-000001");
        job.setChannel(NotificationChannelType.EMAIL);
        job.setToAddress("ops@example.com");
        job.setSubject("Hello");
        job.setBody("Body");
        job.setStatus(NotificationJobStatus.QUEUED);
        job.setScheduledFor(Instant.parse("2026-08-21T00:00:00Z"));
        return job;
    }

    private static NotificationJobResponse sentResponse(NotificationJob job) {
        return new NotificationJobResponse(
                job.getId(),
                job.getTenantId(),
                job.getJobNumber(),
                job.getTemplateId(),
                job.getChannel(),
                job.getToAddress(),
                job.getSubject(),
                job.getBody(),
                NotificationJobStatus.SENT,
                job.getScheduledFor(),
                Instant.parse("2026-08-21T12:00:00Z"),
                null,
                false
        );
    }
}
