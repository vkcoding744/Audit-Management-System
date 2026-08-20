package com.auditplatform.audit.service;

import com.auditplatform.audit.api.AuditItemResponse;
import com.auditplatform.audit.api.AuditResponse;
import com.auditplatform.audit.api.UpdateAuditItemRequest;
import com.auditplatform.audit.api.UpdateExecutionRequest;
import com.auditplatform.audit.domain.AssessmentResult;
import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditChecklistResponse;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.repository.AuditChecklistResponseRepository;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.domain.Checklist;
import com.auditplatform.standards.domain.ChecklistItem;
import com.auditplatform.standards.domain.ChecklistStatus;
import com.auditplatform.standards.service.ChecklistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ExecutionService {

    private final AuditService auditService;
    private final AuditRepository auditRepository;
    private final AuditChecklistResponseRepository responseRepository;
    private final ChecklistService checklistService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public ExecutionService(
            AuditService auditService,
            AuditRepository auditRepository,
            AuditChecklistResponseRepository responseRepository,
            ChecklistService checklistService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.auditService = auditService;
        this.auditRepository = auditRepository;
        this.responseRepository = responseRepository;
        this.checklistService = checklistService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional
    public AuditResponse start(String auditId) {
        Audit audit = auditService.requireAudit(auditId);
        if (audit.getStatus() != AuditStatus.SCHEDULED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only scheduled audits can be started");
        }
        snapshotChecklist(audit);
        audit.setStatus(AuditStatus.IN_PROGRESS);
        audit.setActualStartOn(LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC));
        auditRepository.save(audit);
        auditLogService.record("AUDIT_START", "Audit", audit.getId(), "SCHEDULED", "IN_PROGRESS", null, null);
        return auditService.get(audit.getId());
    }

    @Transactional
    public AuditResponse complete(String auditId) {
        Audit audit = requireInProgress(auditId);
        long unanswered = responseRepository.countByAuditIdAndRequiredIsTrueAndResultAndDeletedAtIsNull(
                audit.getId(),
                AssessmentResult.NOT_ASSESSED
        );
        if (unanswered > 0) {
            throw new ApiException(
                    ErrorCode.SYS_VALIDATION,
                    "Complete requires a result on every required checklist item"
            );
        }
        audit.setStatus(AuditStatus.COMPLETED);
        audit.setActualEndOn(LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC));
        auditRepository.save(audit);
        auditLogService.record("AUDIT_COMPLETE", "Audit", audit.getId(), "IN_PROGRESS", "COMPLETED", null, null);
        return auditService.get(audit.getId());
    }

    @Transactional
    public AuditResponse updateNotes(String auditId, UpdateExecutionRequest request) {
        Audit audit = requireInProgress(auditId);
        if (request.openingNotes() != null) {
            audit.setOpeningNotes(blankToNull(request.openingNotes()));
        }
        if (request.closingNotes() != null) {
            audit.setClosingNotes(blankToNull(request.closingNotes()));
        }
        auditRepository.save(audit);
        return auditService.get(audit.getId());
    }

    @Transactional(readOnly = true)
    public List<AuditItemResponse> listResponses(String auditId) {
        Audit audit = auditService.requireAudit(auditId);
        return responseRepository
                .findByTenantIdAndAuditIdAndDeletedAtIsNullOrderBySortOrderAsc(audit.getTenantId(), audit.getId())
                .stream()
                .map(AuditItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuditItemResponse getResponse(String responseId) {
        return AuditItemResponse.from(requireResponse(responseId));
    }

    @Transactional
    public AuditItemResponse updateResponse(String responseId, UpdateAuditItemRequest request) {
        AuditChecklistResponse response = requireResponse(responseId);
        requireInProgress(response.getAuditId());
        AssessmentResult result = request.result();
        String comment = blankToNull(request.comment());
        if ((result == AssessmentResult.NONCONFORMING || result == AssessmentResult.OBSERVATION) && comment == null) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Nonconformity and observation require a comment");
        }
        response.setResult(result);
        response.setComment(comment);
        if (result == AssessmentResult.NOT_ASSESSED) {
            response.setAssessedBy(null);
            response.setAssessedAt(null);
        } else {
            response.setAssessedBy(isolationService.requirePrincipal().userId());
            response.setAssessedAt(clock.instant());
        }
        responseRepository.save(response);
        return AuditItemResponse.from(response);
    }

    private void snapshotChecklist(Audit audit) {
        if (audit.getChecklistId() == null || audit.getChecklistId().isBlank()) {
            return;
        }
        if (responseRepository.existsByAuditIdAndDeletedAtIsNull(audit.getId())) {
            return;
        }
        Checklist checklist = checklistService.requireChecklist(audit.getChecklistId());
        if (checklist.getStatus() == ChecklistStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Cannot start fieldwork against a draft checklist");
        }
        List<ChecklistItem> items = checklistService.listItems(audit.getChecklistId());
        for (ChecklistItem item : items) {
            AuditChecklistResponse response = new AuditChecklistResponse();
            response.setTenantId(audit.getTenantId());
            response.setAuditId(audit.getId());
            response.setChecklistItemId(item.getId());
            response.setClauseId(item.getClauseId());
            response.setTitle(item.getTitle());
            response.setGuidance(item.getGuidance());
            response.setItemType(item.getItemType());
            response.setRequired(item.isRequired());
            response.setSortOrder(item.getSortOrder());
            response.setResult(AssessmentResult.NOT_ASSESSED);
            responseRepository.save(response);
        }
    }

    private Audit requireInProgress(String auditId) {
        Audit audit = auditService.requireAudit(auditId);
        if (audit.getStatus() != AuditStatus.IN_PROGRESS) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only in-progress audits can be recorded or completed");
        }
        return audit;
    }

    public AuditChecklistResponse requireResponse(String id) {
        AuditChecklistResponse response = responseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Checklist response not found"));
        isolationService.assertCanAccessTenant(response.getTenantId());
        return response;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
