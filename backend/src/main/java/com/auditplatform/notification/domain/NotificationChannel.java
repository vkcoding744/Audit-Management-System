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
@Table(name = "notification_channels")
@Getter
@Setter
public class NotificationChannel extends TenantAwareEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 32)
    private NotificationChannelType channel = NotificationChannelType.EMAIL;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "from_address", length = 255)
    private String fromAddress;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
