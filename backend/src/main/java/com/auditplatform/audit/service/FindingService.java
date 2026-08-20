package com.auditplatform.audit.service;

import com.auditplatform.audit.api.CapaResponse;
import com.auditplatform.audit.api.CreateCapaRequest;
import com.auditplatform.audit.api.CreateFindingRequest;
import com.auditplatform.audit.api.FindingResponse;
import com.auditplatform.audit.api.UpdateCapaRequest;
import com.auditplatform.audit.api.UpdateFindingRequest;
import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditChecklistResponse;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.CapaAction;
import com.auditplatform.audit.domain.CapaStatus;
import com.auditplatform.audit.domain.Finding;
import com.auditplatform.audit.domain.FindingSeverity;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.audit.repository.CapaActionRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.domain.Site;
import com.auditplatform.crm.service.SiteService;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class FindingService {

    private final FindingRepository findingRepository;
    private final CapaActionRepository capaRepository;
    private final AuditNumberService auditNumberService;
    private final AuditService auditService;
    private final ExecutionService executionService;
    private final SiteService siteService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public FindingService(
            FindingRepository findingRepository,
            CapaActionRepository capaRepository,
            AuditNumberService auditNumberService,
            AuditService auditService,
            ExecutionService executionService,
            SiteService siteService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.findingRepository = findingRepository;
        this.capaRepository = capaRepository;
        this.auditNumberService = auditNumberService;
        this.auditService = auditService;
        this.executionService = executionService;
        this.siteService = siteService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<FindingResponse> list(String clientId, FindingStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Finding> page;
        if (clientId != null && !clientId.isBlank()) {
            page = findingRepository.findByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId, pageable);
        } else if (status != null) {
            page = findingRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = findingRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(FindingResponse::summary));
    }

    @Transactional(readOnly = true)
    public List<FindingResponse> listByAudit(String auditId) {
        Audit audit = auditService.requireAudit(auditId);
        return findingRepository
                .findByTenantIdAndAuditIdAndDeletedAtIsNullOrderByFindingNumberAsc(audit.getTenantId(), audit.getId())
                .stream()
                .map(FindingResponse::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public FindingResponse get(String id) {
        Finding finding = requireFinding(id);
        return FindingResponse.from(finding, capas(finding));
    }

    @Transactional
    public FindingResponse create(CreateFindingRequest request) {
        Audit audit = auditService.requireAudit(request.auditId());
        if (audit.getStatus() != AuditStatus.IN_PROGRESS && audit.getStatus() != AuditStatus.COMPLETED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Findings can only be raised on in-progress or completed audits");
        }
        Finding finding = new Finding();
        finding.setTenantId(audit.getTenantId());
        finding.setFindingNumber(auditNumberService.nextFinding(audit.getTenantId()));
        finding.setAuditId(audit.getId());
        finding.setClientId(audit.getClientId());
        finding.setSiteId(resolveSite(audit, request.siteId()));
        finding.setResponseId(resolveResponse(audit, request.responseId()));
        finding.setClauseId(blankToNull(request.clauseId()));
        finding.setTitle(request.title().trim());
        finding.setDescription(request.description().trim());
        finding.setSeverity(request.severity() == null ? FindingSeverity.MINOR : request.severity());
        finding.setStatus(FindingStatus.OPEN);
        finding.setNotes(blankToNull(request.notes()));
        findingRepository.save(finding);
        auditLogService.record("FINDING_CREATE", "Finding", finding.getId(), null, finding.getFindingNumber(), null, null);
        return FindingResponse.from(finding, List.of());
    }

    @Transactional
    public FindingResponse update(String id, UpdateFindingRequest request) {
        Finding finding = requireOpen(id);
        if (request.title() != null && !request.title().isBlank()) {
            finding.setTitle(request.title().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            finding.setDescription(request.description().trim());
        }
        if (request.severity() != null) {
            finding.setSeverity(request.severity());
        }
        if (request.notes() != null) {
            finding.setNotes(blankToNull(request.notes()));
        }
        findingRepository.save(finding);
        return FindingResponse.from(finding, capas(finding));
    }

    @Transactional
    public FindingResponse close(String id) {
        Finding finding = requireOpen(id);
        if (finding.getSeverity() == FindingSeverity.MAJOR || finding.getSeverity() == FindingSeverity.MINOR) {
            if (capaRepository.countByFindingIdAndDeletedAtIsNull(finding.getId()) == 0) {
                throw new ApiException(ErrorCode.SYS_VALIDATION, "Major and minor findings require at least one CAPA before close");
            }
            if (capaRepository.existsByFindingIdAndStatusAndDeletedAtIsNull(finding.getId(), CapaStatus.OPEN)) {
                throw new ApiException(ErrorCode.SYS_VALIDATION, "Open CAPA must be completed before the finding can be closed");
            }
        }
        finding.setStatus(FindingStatus.CLOSED);
        finding.setClosedOn(today());
        findingRepository.save(finding);
        auditLogService.record("FINDING_CLOSE", "Finding", finding.getId(), "OPEN", "CLOSED", null, null);
        return FindingResponse.from(finding, capas(finding));
    }

    @Transactional
    public CapaResponse addCapa(String findingId, CreateCapaRequest request) {
        Finding finding = requireOpen(findingId);
        CapaAction capa = new CapaAction();
        capa.setTenantId(finding.getTenantId());
        capa.setCapaNumber(auditNumberService.nextCapa(finding.getTenantId()));
        capa.setFindingId(finding.getId());
        capa.setDescription(request.description().trim());
        capa.setDueOn(request.dueOn());
        capa.setStatus(CapaStatus.OPEN);
        capa.setNotes(blankToNull(request.notes()));
        capaRepository.save(capa);
        auditLogService.record("CAPA_CREATE", "CapaAction", capa.getId(), null, capa.getCapaNumber(), null, null);
        return CapaResponse.from(capa);
    }

    @Transactional
    public CapaResponse updateCapa(String capaId, UpdateCapaRequest request) {
        CapaAction capa = requireOpenCapa(capaId);
        if (request.description() != null && !request.description().isBlank()) {
            capa.setDescription(request.description().trim());
        }
        if (request.dueOn() != null) {
            capa.setDueOn(request.dueOn());
        }
        if (request.notes() != null) {
            capa.setNotes(blankToNull(request.notes()));
        }
        capaRepository.save(capa);
        return CapaResponse.from(capa);
    }

    @Transactional
    public CapaResponse completeCapa(String capaId) {
        CapaAction capa = requireOpenCapa(capaId);
        capa.setStatus(CapaStatus.COMPLETED);
        capa.setCompletedOn(today());
        capaRepository.save(capa);
        auditLogService.record("CAPA_COMPLETE", "CapaAction", capa.getId(), "OPEN", "COMPLETED", null, null);
        return CapaResponse.from(capa);
    }

    @Transactional(readOnly = true)
    public List<CapaResponse> listCapa(String findingId) {
        Finding finding = requireFinding(findingId);
        return capas(finding);
    }

    public Finding requireFinding(String id) {
        Finding finding = findingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Finding not found"));
        isolationService.assertCanAccessTenant(finding.getTenantId());
        return finding;
    }

    private Finding requireOpen(String id) {
        Finding finding = requireFinding(id);
        if (finding.getStatus() != FindingStatus.OPEN) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Closed findings cannot be edited");
        }
        return finding;
    }

    private CapaAction requireOpenCapa(String capaId) {
        CapaAction capa = capaRepository.findByIdAndDeletedAtIsNull(capaId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "CAPA not found"));
        isolationService.assertCanAccessTenant(capa.getTenantId());
        requireOpen(capa.getFindingId());
        if (capa.getStatus() != CapaStatus.OPEN) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Completed CAPA cannot be edited");
        }
        return capa;
    }

    private List<CapaResponse> capas(Finding finding) {
        return capaRepository
                .findByTenantIdAndFindingIdAndDeletedAtIsNullOrderByDueOnAsc(finding.getTenantId(), finding.getId())
                .stream()
                .map(CapaResponse::from)
                .toList();
    }

    private String resolveSite(Audit audit, String siteId) {
        if (siteId == null || siteId.isBlank()) {
            return null;
        }
        Site site = siteService.requireSite(siteId);
        if (!audit.getClientId().equals(site.getClientId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Site does not belong to the audit client");
        }
        return site.getId();
    }

    private String resolveResponse(Audit audit, String responseId) {
        if (responseId == null || responseId.isBlank()) {
            return null;
        }
        AuditChecklistResponse response = executionService.requireResponse(responseId);
        if (!audit.getId().equals(response.getAuditId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Checklist response does not belong to this audit");
        }
        return response.getId();
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
