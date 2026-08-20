package com.auditplatform.crm.metrics;

import org.springframework.stereotype.Component;

/**
 * Later phases replace this adapter with queries against audit, certification, finance,
 * document, and governance tables. Returning zeros is accurate while those tables do not exist.
 */
@Component
public class EmptyClientOperationalMetrics implements ClientOperationalMetricsPort {

    @Override
    public ClientOperationalMetrics load(String tenantId, String clientId) {
        return ClientOperationalMetrics.empty();
    }
}
