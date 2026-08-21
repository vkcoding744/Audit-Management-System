package com.auditplatform.auditlog.api;

import com.auditplatform.auditlog.domain.AuditLog;

import java.time.Instant;

public record AuditLogResponse(
        String id,
        String tenantId,
        String userId,
        String action,
        String entityType,
        String entityId,
        String oldValue,
        String newValue,
        String ipAddress,
        String userAgent,
        String correlationId,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getTenantId(),
                log.getUserId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getOldValue(),
                log.getNewValue(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getCorrelationId(),
                log.getCreatedAt()
        );
    }
}
