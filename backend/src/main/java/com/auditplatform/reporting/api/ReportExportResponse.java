package com.auditplatform.reporting.api;

import com.auditplatform.reporting.domain.ReportExport;
import com.auditplatform.reporting.domain.ReportExportStatus;
import com.auditplatform.reporting.domain.ReportFormat;

import java.time.Instant;

public record ReportExportResponse(
        String id,
        String tenantId,
        String definitionId,
        String exportNumber,
        ReportFormat format,
        ReportExportStatus status,
        Integer rowCount,
        Long byteSize,
        String errorMessage,
        Instant completedAt
) {
    public static ReportExportResponse from(ReportExport export) {
        return new ReportExportResponse(
                export.getId(),
                export.getTenantId(),
                export.getDefinitionId(),
                export.getExportNumber(),
                export.getFormat(),
                export.getStatus(),
                export.getRowCount(),
                export.getByteSize(),
                export.getErrorMessage(),
                export.getCompletedAt()
        );
    }
}
