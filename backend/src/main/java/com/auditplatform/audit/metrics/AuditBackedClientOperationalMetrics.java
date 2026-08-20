package com.auditplatform.audit.metrics;

import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.crm.metrics.ClientOperationalMetrics;
import com.auditplatform.crm.metrics.ClientOperationalMetricsPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class AuditBackedClientOperationalMetrics implements ClientOperationalMetricsPort {

    private static final List<AuditStatus> UPCOMING = List.of(AuditStatus.PLANNED, AuditStatus.SCHEDULED, AuditStatus.IN_PROGRESS);

    private final AuditRepository auditRepository;

    public AuditBackedClientOperationalMetrics(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    public ClientOperationalMetrics load(String tenantId, String clientId) {
        long upcoming = auditRepository.countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(tenantId, clientId, UPCOMING);
        long completed = auditRepository.countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
                tenantId,
                clientId,
                List.of(AuditStatus.COMPLETED)
        );
        return new ClientOperationalMetrics(upcoming, completed, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
