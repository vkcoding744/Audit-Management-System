package com.auditplatform.reporting.api;

import com.auditplatform.reporting.domain.ReportDataset;
import com.auditplatform.reporting.domain.ReportFormat;
import jakarta.validation.constraints.Size;

public record UpdateReportRequest(
        @Size(max = 255) String name,
        @Size(max = 512) String description,
        ReportDataset dataset,
        ReportFormat format,
        @Size(max = 64) String statusFilter
) {
}
