package com.auditplatform.audit.metrics;

import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.CapaStatus;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.CapaActionRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.crm.metrics.ClientOperationalMetrics;
import com.auditplatform.crm.metrics.ClientOperationalMetricsPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
@Primary
public class AuditBackedClientOperationalMetrics implements ClientOperationalMetricsPort {

    private static final List<AuditStatus> UPCOMING = List.of(AuditStatus.PLANNED, AuditStatus.SCHEDULED, AuditStatus.IN_PROGRESS);

    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final CapaActionRepository capaRepository;
    private final Clock clock;

    public AuditBackedClientOperationalMetrics(
            AuditRepository auditRepository,
            FindingRepository findingRepository,
            CapaActionRepository capaRepository,
            Clock clock
    ) {
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.capaRepository = capaRepository;
        this.clock = clock;
    }

    @Override
    public ClientOperationalMetrics load(String tenantId, String clientId) {
        long upcoming = auditRepository.countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(tenantId, clientId, UPCOMING);
        long completed = auditRepository.countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
                tenantId,
                clientId,
                List.of(AuditStatus.COMPLETED)
        );
        long openFindings = findingRepository.countByTenantIdAndClientIdAndStatusAndDeletedAtIsNull(
                tenantId,
                clientId,
                FindingStatus.OPEN
        );
        long overdueCapa = capaRepository.countOverdueForClient(
                tenantId,
                clientId,
                CapaStatus.OPEN,
                LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC)
        );
        return new ClientOperationalMetrics(upcoming, completed, openFindings, overdueCapa, 0, 0, 0, 0, 0, 0);
    }
}
