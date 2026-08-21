package com.auditplatform.notification.api;

public record NotificationDispatchResponse(
        int sent,
        int skipped,
        int failed,
        int considered
) {
}
