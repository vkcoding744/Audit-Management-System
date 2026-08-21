package com.auditplatform.notification.api;

import com.auditplatform.notification.domain.NotificationChannelType;
import com.auditplatform.notification.domain.NotificationJob;
import com.auditplatform.notification.domain.NotificationJobStatus;

import java.time.Instant;

public record NotificationJobResponse(
        String id,
        String tenantId,
        String jobNumber,
        String templateId,
        NotificationChannelType channel,
        String toAddress,
        String subject,
        String body,
        NotificationJobStatus status,
        Instant scheduledFor,
        Instant sentAt,
        String errorMessage,
        boolean due
) {
    public static NotificationJobResponse from(NotificationJob job, boolean due) {
        return new NotificationJobResponse(
                job.getId(),
                job.getTenantId(),
                job.getJobNumber(),
                job.getTemplateId(),
                job.getChannel(),
                job.getToAddress(),
                job.getSubject(),
                job.getBody(),
                job.getStatus(),
                job.getScheduledFor(),
                job.getSentAt(),
                job.getErrorMessage(),
                due
        );
    }
}
