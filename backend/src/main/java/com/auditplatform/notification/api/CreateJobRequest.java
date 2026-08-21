package com.auditplatform.notification.api;

import com.auditplatform.notification.domain.NotificationChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

public record CreateJobRequest(
        String templateId,
        NotificationChannelType channel,
        @NotBlank @Size(max = 255) String toAddress,
        @Size(max = 255) String subject,
        String body,
        Instant scheduledFor,
        Map<String, String> variables
) {
}
