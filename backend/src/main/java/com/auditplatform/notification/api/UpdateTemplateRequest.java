package com.auditplatform.notification.api;

import com.auditplatform.notification.domain.NotificationChannelType;
import com.auditplatform.notification.domain.NotificationEventType;
import jakarta.validation.constraints.Size;

public record UpdateTemplateRequest(
        @Size(max = 255) String name,
        NotificationChannelType channel,
        NotificationEventType eventType,
        @Size(max = 255) String subject,
        String body
) {
}
