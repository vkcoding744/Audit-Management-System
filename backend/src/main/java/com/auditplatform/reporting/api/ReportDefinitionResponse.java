package com.auditplatform.reporting.api;

import com.auditplatform.reporting.domain.ReportDataset;
import com.auditplatform.reporting.domain.ReportDefinition;
import com.auditplatform.reporting.domain.ReportDefinitionStatus;
import com.auditplatform.reporting.domain.ReportFormat;

public record ReportDefinitionResponse(
        String id,
        String tenantId,
        String reportNumber,
        String name,
        String description,
        ReportDataset dataset,
        ReportFormat format,
        String statusFilter,
        ReportDefinitionStatus status
) {
    public static ReportDefinitionResponse from(ReportDefinition definition) {
        return new ReportDefinitionResponse(
                definition.getId(),
                definition.getTenantId(),
                definition.getReportNumber(),
                definition.getName(),
                definition.getDescription(),
                definition.getDataset(),
                definition.getFormat(),
                definition.getStatusFilter(),
                definition.getStatus()
        );
    }
}
