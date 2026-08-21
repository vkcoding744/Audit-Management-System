package com.auditplatform.notification.api;

import com.auditplatform.notification.domain.NotificationChannel;
import com.auditplatform.notification.domain.NotificationChannelType;

public record ChannelResponse(
        String id,
        String tenantId,
        NotificationChannelType channel,
        boolean enabled,
        String fromAddress
) {
    public static ChannelResponse from(NotificationChannel channel) {
        return new ChannelResponse(
                channel.getId(),
                channel.getTenantId(),
                channel.getChannel(),
                channel.isEnabled(),
                channel.getFromAddress()
        );
    }
}
