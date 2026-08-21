package com.auditplatform.notification.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.common.tenant.TenantContext;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.notification.domain.NotificationChannelType;
import com.auditplatform.notification.domain.NotificationJob;
import com.auditplatform.notification.domain.NotificationJobStatus;
import com.auditplatform.notification.email.OutboundEmailPort;
import com.auditplatform.notification.repository.NotificationJobRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationJobServiceTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-21T12:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void sendBlockedWhenJobAlreadySent() {
        bindUser();
        NotificationJob job = queued();
        job.setStatus(NotificationJobStatus.SENT);
        NotificationJobRepository jobs = mock(NotificationJobRepository.class);
        when(jobs.findByIdAndDeletedAtIsNull("job-1")).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service(jobs).send("job-1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("Only queued jobs can be sent");
                });
    }

    @Test
    void deliverQueuedJobDoesNotRequirePrincipal() {
        NotificationJob job = queued();
        NotificationJobRepository jobs = mock(NotificationJobRepository.class);
        when(jobs.findByIdAndDeletedAtIsNull("job-1")).thenReturn(Optional.of(job));
        when(jobs.save(job)).thenReturn(job);
        OutboundEmailPort email = mock(OutboundEmailPort.class);
        NotificationChannelService channels = mock(NotificationChannelService.class);

        assertThat(service(jobs, channels, email).deliverQueuedJob("job-1").status())
                .isEqualTo(NotificationJobStatus.SENT);
        verify(email).send("ops@example.com", "Hello", "Body");
    }

    @Test
    void dueIsTrueWhenQueuedJobIsPastScheduledFor() {
        bindUser();
        NotificationJob job = queued();
        job.setScheduledFor(Instant.parse("2026-08-21T00:00:00Z"));
        NotificationJobRepository jobs = mock(NotificationJobRepository.class);
        when(jobs.findByIdAndDeletedAtIsNull("job-1")).thenReturn(Optional.of(job));

        assertThat(service(jobs).get("job-1").due()).isTrue();
    }

    @Test
    void rendererSubstitutesPlaceholders() {
        assertThat(TemplateRenderer.render("Hello {{ name }}", Map.of("name", "Ada"))).isEqualTo("Hello Ada");
    }

    private NotificationJobService service(NotificationJobRepository jobs) {
        return service(jobs, mock(NotificationChannelService.class), mock(OutboundEmailPort.class));
    }

    private NotificationJobService service(
            NotificationJobRepository jobs,
            NotificationChannelService channels,
            OutboundEmailPort email
    ) {
        return new NotificationJobService(
                jobs,
                mock(NotificationNumberService.class),
                mock(NotificationTemplateService.class),
                channels,
                email,
                isolationService,
                mock(AuditLogService.class),
                clock
        );
    }

    private static NotificationJob queued() {
        NotificationJob job = new NotificationJob();
        ReflectionTestUtils.setField(job, "id", "job-1");
        job.setTenantId("tenant-a");
        job.setJobNumber("NTF-000001");
        job.setChannel(NotificationChannelType.EMAIL);
        job.setToAddress("ops@example.com");
        job.setSubject("Hello");
        job.setBody("Body");
        job.setStatus(NotificationJobStatus.QUEUED);
        return job;
    }

    private static void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("NOTIFICATION_UPDATE")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
