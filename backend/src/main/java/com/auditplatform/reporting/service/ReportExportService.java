package com.auditplatform.reporting.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.document.storage.ObjectStorageException;
import com.auditplatform.document.storage.ObjectStoragePort;
import com.auditplatform.document.storage.StorageKeys;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.reporting.api.ReportExportContent;
import com.auditplatform.reporting.api.ReportExportResponse;
import com.auditplatform.reporting.domain.ReportDefinition;
import com.auditplatform.reporting.domain.ReportDefinitionStatus;
import com.auditplatform.reporting.domain.ReportExport;
import com.auditplatform.reporting.domain.ReportExportStatus;
import com.auditplatform.reporting.repository.ReportExportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ReportExportService {

    private final ReportExportRepository exportRepository;
    private final ReportDefinitionService definitionService;
    private final ReportNumberService numberService;
    private final ReportDatasetQueryService datasetQueryService;
    private final ReportRenderer renderer;
    private final ObjectStoragePort objectStorage;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public ReportExportService(
            ReportExportRepository exportRepository,
            ReportDefinitionService definitionService,
            ReportNumberService numberService,
            ReportDatasetQueryService datasetQueryService,
            ReportRenderer renderer,
            ObjectStoragePort objectStorage,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.exportRepository = exportRepository;
        this.definitionService = definitionService;
        this.numberService = numberService;
        this.datasetQueryService = datasetQueryService;
        this.renderer = renderer;
        this.objectStorage = objectStorage;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportExportResponse> list(ReportExportStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<ReportExport> page = status == null
                ? exportRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : exportRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        return PageResponse.from(page.map(ReportExportResponse::from));
    }

    @Transactional(readOnly = true)
    public ReportExportResponse get(String id) {
        return ReportExportResponse.from(requireExport(id));
    }

    @Transactional
    public ReportExportResponse run(String definitionId) {
        ReportDefinition definition = definitionService.requireDefinition(definitionId);
        if (definition.getStatus() == ReportDefinitionStatus.ARCHIVED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Archived reports cannot be run");
        }
        String tenantId = definition.getTenantId();
        ReportExport export = new ReportExport();
        export.setTenantId(tenantId);
        export.setDefinitionId(definition.getId());
        export.setExportNumber(numberService.nextExport(tenantId));
        export.setFormat(definition.getFormat());
        export.setStatus(ReportExportStatus.QUEUED);
        exportRepository.saveAndFlush(export);
        try {
            ReportDatasetQueryService.DatasetSnapshot snapshot = datasetQueryService.query(tenantId, definition);
            byte[] bytes = renderer.render(definition.getFormat(), snapshot.columns(), snapshot.rows());
            String storageKey = StorageKeys.forReportExport(tenantId, export.getId(), definition.getFormat().fileExtension());
            objectStorage.put(storageKey, bytes, definition.getFormat().contentType());
            export.setStorageKey(storageKey);
            export.setContentType(definition.getFormat().contentType());
            export.setRowCount(snapshot.rows().size());
            export.setByteSize((long) bytes.length);
            export.setStatus(ReportExportStatus.COMPLETED);
            export.setCompletedAt(Instant.now(clock));
            export.setErrorMessage(null);
        } catch (RuntimeException ex) {
            export.setStatus(ReportExportStatus.FAILED);
            export.setCompletedAt(Instant.now(clock));
            export.setErrorMessage(truncate(ex.getMessage()));
        }
        exportRepository.save(export);
        auditLogService.record("REPORT_EXPORT_RUN", "ReportExport", export.getId(), null, export.getExportNumber(), null, null);
        return ReportExportResponse.from(export);
    }

    @Transactional
    public ReportExportResponse cancel(String id) {
        ReportExport export = requireExport(id);
        if (export.getStatus() != ReportExportStatus.QUEUED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only queued exports can be cancelled");
        }
        export.setStatus(ReportExportStatus.CANCELLED);
        exportRepository.save(export);
        return ReportExportResponse.from(export);
    }

    @Transactional(readOnly = true)
    public ReportExportContent download(String id) {
        ReportExport export = requireExport(id);
        if (export.getStatus() != ReportExportStatus.COMPLETED || export.getStorageKey() == null) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only completed exports can be downloaded");
        }
        try {
            return new ReportExportContent(
                    export.getExportNumber() + "." + export.getFormat().fileExtension(),
                    export.getContentType() == null ? export.getFormat().contentType() : export.getContentType(),
                    export.getByteSize() == null ? -1 : export.getByteSize(),
                    objectStorage.open(export.getStorageKey())
            );
        } catch (ObjectStorageException ex) {
            throw new ApiException(ErrorCode.SYS_INTERNAL, "Could not read export content");
        }
    }

    public ReportExport requireExport(String id) {
        ReportExport export = exportRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Report export not found"));
        isolationService.assertCanAccessTenant(export.getTenantId());
        return export;
    }

    private static String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "Export failed";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
