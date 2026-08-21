package com.auditplatform.notification.service;

import com.auditplatform.common.exception.ApiException;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.notification.api.NotificationDispatchResponse;
import com.auditplatform.notification.api.NotificationJobResponse;
import com.auditplatform.notification.domain.NotificationJob;
import com.auditplatform.notification.domain.NotificationJobStatus;
import com.auditplatform.notification.repository.NotificationJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationJobRepository jobRepository;
    private final NotificationJobService jobService;
    private final IsolationService isolationService;
    private final Clock clock;
    private final int batchSize;

    public NotificationDispatchService(
            NotificationJobRepository jobRepository,
            NotificationJobService jobService,
            IsolationService isolationService,
            Clock clock,
            @Value("${audit.notifications.dispatch-batch-size:50}") int batchSize
    ) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.isolationService = isolationService;
        this.clock = clock;
        this.batchSize = batchSize;
    }

    public NotificationDispatchResponse dispatchForCurrentTenant() {
        String tenantId = isolationService.requireTenantScope();
        return dispatchDueJobs(tenantId, batchSize);
    }

    public NotificationDispatchResponse dispatchDueJobs(String tenantId, int limit) {
        int size = Math.max(1, Math.min(limit, 200));
        List<NotificationJob> due = jobRepository.findDueQueued(
                clock.instant(),
                tenantId,
                PageRequest.of(0, size)
        );
        int sent = 0;
        int skipped = 0;
        int failed = 0;
        for (NotificationJob job : due) {
            try {
                NotificationJobResponse result = jobService.deliverQueuedJob(job.getId());
                if (result.status() == NotificationJobStatus.SENT) {
                    sent++;
                } else if (result.status() == NotificationJobStatus.FAILED) {
                    failed++;
                } else {
                    skipped++;
                }
            } catch (ApiException ex) {
                skipped++;
            } catch (RuntimeException ex) {
                log.warn("Notification dispatch failed for job {}", job.getId(), ex);
                failed++;
            }
        }
        return new NotificationDispatchResponse(sent, skipped, failed, due.size());
    }
}
