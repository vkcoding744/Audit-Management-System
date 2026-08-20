package com.auditplatform.crm.metrics;

public interface ClientOperationalMetricsPort {

    ClientOperationalMetrics load(String tenantId, String clientId);
}
