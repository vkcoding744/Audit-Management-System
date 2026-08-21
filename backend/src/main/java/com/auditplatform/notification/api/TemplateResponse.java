package com.auditplatform.notification.api;

import com.auditplatform.notification.domain.NotificationChannelType;
import com.auditplatform.notification.domain.NotificationEventType;
import com.auditplatform.notification.domain.NotificationTemplate;
import com.auditplatform.notification.domain.TemplateStatus;

public record TemplateResponse(
        String id,
        String tenantId,
        String code,
        String name,
        NotificationChannelType channel,
        NotificationEventType eventType,
        String subject,
        String body,
        TemplateStatus status
) {
    public static TemplateResponse from(NotificationTemplate template) {
        return new TemplateResponse(
                template.getId(),
                template.getTenantId(),
                template.getCode(),
                template.getName(),
                template.getChannel(),
                template.getEventType(),
                template.getSubject(),
                template.getBody(),
                template.getStatus()
        );
    }
}
