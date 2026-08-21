package com.auditplatform.reporting.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.reporting.api.CreateReportRequest;
import com.auditplatform.reporting.api.ReportDefinitionResponse;
import com.auditplatform.reporting.api.UpdateReportRequest;
import com.auditplatform.reporting.domain.ReportDataset;
import com.auditplatform.reporting.domain.ReportDefinition;
import com.auditplatform.reporting.domain.ReportDefinitionStatus;
import com.auditplatform.reporting.domain.ReportFormat;
import com.auditplatform.reporting.repository.ReportDefinitionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportDefinitionService {

    private final ReportDefinitionRepository definitionRepository;
    private final ReportNumberService numberService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public ReportDefinitionService(
            ReportDefinitionRepository definitionRepository,
            ReportNumberService numberService,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.definitionRepository = definitionRepository;
        this.numberService = numberService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportDefinitionResponse> list(ReportDefinitionStatus status, ReportDataset dataset, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<ReportDefinition> page;
        if (status != null && dataset != null) {
            page = definitionRepository.findByTenantIdAndStatusAndDatasetAndDeletedAtIsNull(tenantId, status, dataset, pageable);
        } else if (status != null) {
            page = definitionRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else if (dataset != null) {
            page = definitionRepository.findByTenantIdAndDatasetAndDeletedAtIsNull(tenantId, dataset, pageable);
        } else {
            page = definitionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(ReportDefinitionResponse::from));
    }

    @Transactional(readOnly = true)
    public ReportDefinitionResponse get(String id) {
        return ReportDefinitionResponse.from(requireDefinition(id));
    }

    @Transactional
    public ReportDefinitionResponse create(CreateReportRequest request) {
        String tenantId = isolationService.requireTenantScope();
        request.dataset().validateStatusFilter(blankToNull(request.statusFilter()));
        ReportDefinition definition = new ReportDefinition();
        definition.setTenantId(tenantId);
        definition.setReportNumber(numberService.nextReport(tenantId));
        definition.setName(request.name().trim());
        definition.setDescription(blankToNull(request.description()));
        definition.setDataset(request.dataset());
        definition.setFormat(request.format() == null ? ReportFormat.CSV : request.format());
        definition.setStatusFilter(blankToNull(request.statusFilter()));
        definition.setStatus(ReportDefinitionStatus.DRAFT);
        definitionRepository.save(definition);
        auditLogService.record("REPORT_CREATE", "ReportDefinition", definition.getId(), null, definition.getReportNumber(), null, null);
        return ReportDefinitionResponse.from(definition);
    }

    @Transactional
    public ReportDefinitionResponse update(String id, UpdateReportRequest request) {
        ReportDefinition definition = requireDefinition(id);
        assertDraft(definition);
        if (request.name() != null && !request.name().isBlank()) {
            definition.setName(request.name().trim());
        }
        if (request.description() != null) {
            definition.setDescription(blankToNull(request.description()));
        }
        if (request.dataset() != null) {
            definition.setDataset(request.dataset());
        }
        if (request.format() != null) {
            definition.setFormat(request.format());
        }
        if (request.statusFilter() != null) {
            definition.setStatusFilter(blankToNull(request.statusFilter()));
        }
        definition.getDataset().validateStatusFilter(definition.getStatusFilter());
        definitionRepository.save(definition);
        return ReportDefinitionResponse.from(definition);
    }

    @Transactional
    public ReportDefinitionResponse publish(String id) {
        ReportDefinition definition = requireDefinition(id);
        if (definition.getStatus() != ReportDefinitionStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft reports can be published");
        }
        definition.setStatus(ReportDefinitionStatus.ACTIVE);
        definitionRepository.save(definition);
        return ReportDefinitionResponse.from(definition);
    }

    @Transactional
    public ReportDefinitionResponse archive(String id) {
        ReportDefinition definition = requireDefinition(id);
        if (definition.getStatus() == ReportDefinitionStatus.ARCHIVED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Report is already archived");
        }
        definition.setStatus(ReportDefinitionStatus.ARCHIVED);
        definitionRepository.save(definition);
        return ReportDefinitionResponse.from(definition);
    }

    public ReportDefinition requireDefinition(String id) {
        ReportDefinition definition = definitionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Report definition not found"));
        isolationService.assertCanAccessTenant(definition.getTenantId());
        return definition;
    }

    private static void assertDraft(ReportDefinition definition) {
        if (definition.getStatus() != ReportDefinitionStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft reports can be updated");
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
