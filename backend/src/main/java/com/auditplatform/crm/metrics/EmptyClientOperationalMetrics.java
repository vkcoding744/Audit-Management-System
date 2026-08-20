package com.auditplatform.crm.metrics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Fallback while certification/finance tables are absent. Phase 6 supplies a real adapter for audit counts.
 */
@Component
@ConditionalOnMissingBean(ClientOperationalMetricsPort.class)
public class EmptyClientOperationalMetrics implements ClientOperationalMetricsPort {

    @Override
    public ClientOperationalMetrics load(String tenantId, String clientId) {
        return ClientOperationalMetrics.empty();
    }
}
