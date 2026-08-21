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
@Table(name = "report_exports")
@Getter
@Setter
public class ReportExport extends TenantAwareEntity {

    @Column(name = "definition_id", nullable = false, length = 36)
    private String definitionId;

    @Column(name = "export_number", nullable = false, length = 32)
    private String exportNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 16)
    private ReportFormat format = ReportFormat.CSV;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReportExportStatus status = ReportExportStatus.QUEUED;

    @Column(name = "storage_key", length = 512)
    private String storageKey;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "byte_size")
    private Long byteSize;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
