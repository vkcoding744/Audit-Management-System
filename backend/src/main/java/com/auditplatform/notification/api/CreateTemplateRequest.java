package com.auditplatform.notification.api;

import com.auditplatform.notification.domain.NotificationChannelType;
import com.auditplatform.notification.domain.NotificationEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTemplateRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 255) String name,
        NotificationChannelType channel,
        NotificationEventType eventType,
        @NotBlank @Size(max = 255) String subject,
        @NotBlank String body
) {
}
