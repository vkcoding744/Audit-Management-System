package com.auditplatform.notification.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notification_jobs")
@Getter
@Setter
public class NotificationJob extends TenantAwareEntity {

    @Column(name = "job_number", nullable = false, length = 32)
    private String jobNumber;

    @Column(name = "template_id", length = 36)
    private String templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 32)
    private NotificationChannelType channel = NotificationChannelType.EMAIL;

    @Column(name = "to_address", nullable = false, length = 255)
    private String toAddress;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private NotificationJobStatus status = NotificationJobStatus.QUEUED;

    @Column(name = "scheduled_for")
    private Instant scheduledFor;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
