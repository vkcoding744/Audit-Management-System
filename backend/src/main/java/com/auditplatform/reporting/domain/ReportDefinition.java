package com.auditplatform.reporting.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "report_definitions")
@Getter
@Setter
public class ReportDefinition extends TenantAwareEntity {

    @Column(name = "report_number", nullable = false, length = 32)
    private String reportNumber;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 512)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "dataset", nullable = false, length = 32)
    private ReportDataset dataset;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 16)
    private ReportFormat format = ReportFormat.CSV;

    @Column(name = "status_filter", length = 64)
    private String statusFilter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReportDefinitionStatus status = ReportDefinitionStatus.DRAFT;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
