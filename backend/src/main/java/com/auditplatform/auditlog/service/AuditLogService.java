package com.auditplatform.auditlog.service;

import com.auditplatform.auditlog.domain.AuditLog;
import com.auditplatform.auditlog.repository.AuditLogRepository;
import com.auditplatform.common.tenant.TenantContext;
import com.auditplatform.common.web.CorrelationId;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            String action,
            String entityType,
            String entityId,
            String oldValue,
            String newValue,
            String ipAddress,
            String userAgent
    ) {
        AuditLog log = new AuditLog();
        log.setTenantId(TenantContext.getTenantId());
        log.setUserId(TenantContext.getUserId());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setCorrelationId(MDC.get(CorrelationId.MDC_KEY));
        auditLogRepository.save(log);
    }
}
