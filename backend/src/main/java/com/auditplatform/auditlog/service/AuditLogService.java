package com.auditplatform.auditlog.service;

import com.auditplatform.auditlog.api.AuditLogResponse;
import com.auditplatform.auditlog.domain.AuditLog;
import com.auditplatform.auditlog.repository.AuditLogRepository;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.tenant.TenantContext;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.identity.service.IsolationService;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final IsolationService isolationService;

    public AuditLogService(AuditLogRepository auditLogRepository, IsolationService isolationService) {
        this.auditLogRepository = auditLogRepository;
        this.isolationService = isolationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(String action, String entityType, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        String actionFilter = blankToNull(action);
        String typeFilter = blankToNull(entityType);
        Page<AuditLog> page;
        if (actionFilter != null && typeFilter != null) {
            page = auditLogRepository.findByTenantIdAndActionAndEntityTypeOrderByCreatedAtDesc(
                    tenantId, actionFilter, typeFilter, pageable);
        } else if (actionFilter != null) {
            page = auditLogRepository.findByTenantIdAndActionOrderByCreatedAtDesc(tenantId, actionFilter, pageable);
        } else if (typeFilter != null) {
            page = auditLogRepository.findByTenantIdAndEntityTypeOrderByCreatedAtDesc(tenantId, typeFilter, pageable);
        } else {
            page = auditLogRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
        }
        return PageResponse.from(page.map(AuditLogResponse::from));
    }

    @Transactional(readOnly = true)
    public AuditLogResponse get(String id) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Audit log not found"));
        isolationService.assertCanAccessTenant(log.getTenantId());
        return AuditLogResponse.from(log);
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

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
