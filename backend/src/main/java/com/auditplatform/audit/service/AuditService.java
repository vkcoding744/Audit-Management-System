package com.auditplatform.audit.service;

import com.auditplatform.audit.api.AddAuditSiteRequest;
import com.auditplatform.audit.api.AssignmentResponse;
import com.auditplatform.audit.api.AuditResponse;
import com.auditplatform.audit.api.AuditSiteResponse;
import com.auditplatform.audit.api.CreateAuditRequest;
import com.auditplatform.audit.api.UpdateAuditRequest;
import com.auditplatform.audit.domain.AssignmentRole;
import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditProgramme;
import com.auditplatform.audit.domain.AuditSite;
import com.auditplatform.audit.domain.AuditStage;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.AuditType;
import com.auditplatform.audit.domain.ProgrammeStatus;
import com.auditplatform.audit.repository.AuditAssignmentRepository;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.AuditSiteRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.domain.Site;
import com.auditplatform.crm.service.SiteService;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.domain.Checklist;
import com.auditplatform.standards.service.ChecklistService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuditService {

    private final AuditRepository auditRepository;
    private final AuditSiteRepository auditSiteRepository;
    private final AuditAssignmentRepository assignmentRepository;
    private final AuditNumberService auditNumberService;
    private final ProgrammeService programmeService;
    private final SiteService siteService;
    private final ChecklistService checklistService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public AuditService(
            AuditRepository auditRepository,
            AuditSiteRepository auditSiteRepository,
            AuditAssignmentRepository assignmentRepository,
            AuditNumberService auditNumberService,
            ProgrammeService programmeService,
            SiteService siteService,
            ChecklistService checklistService,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.auditRepository = auditRepository;
        this.auditSiteRepository = auditSiteRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditNumberService = auditNumberService;
        this.programmeService = programmeService;
        this.siteService = siteService;
        this.checklistService = checklistService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditResponse> list(String clientId, AuditStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Audit> page;
        if (clientId != null && !clientId.isBlank()) {
            page = auditRepository.findByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId, pageable);
        } else if (status != null) {
            page = auditRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = auditRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(AuditResponse::summary));
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> listByProgramme(String programmeId) {
        AuditProgramme programme = programmeService.requireProgramme(programmeId);
        return auditRepository
                .findByTenantIdAndProgrammeIdAndDeletedAtIsNullOrderByPlannedStartOnAsc(programme.getTenantId(), programme.getId())
                .stream()
                .map(AuditResponse::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuditResponse get(String id) {
        return toDetail(requireAudit(id));
    }

    @Transactional
    public AuditResponse create(CreateAuditRequest request) {
        AuditProgramme programme = programmeService.requireProgramme(request.programmeId());
        if (programme.getStatus() == ProgrammeStatus.CANCELLED || programme.getStatus() == ProgrammeStatus.COMPLETED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Cannot add audits to a completed or cancelled programme");
        }
        if (request.plannedStartOn() != null && request.plannedEndOn() != null
                && request.plannedEndOn().isBefore(request.plannedStartOn())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "plannedEndOn cannot be before plannedStartOn");
        }
        Audit audit = new Audit();
        audit.setTenantId(programme.getTenantId());
        audit.setAuditNumber(auditNumberService.nextAudit(programme.getTenantId()));
        audit.setProgrammeId(programme.getId());
        audit.setClientId(programme.getClientId());
        audit.setSchemeId(programme.getSchemeId());
        audit.setStandardId(programme.getStandardId());
        audit.setChecklistId(resolveChecklist(programme, request.checklistId()));
        audit.setName(request.name().trim());
        audit.setAuditType(request.auditType() == null ? AuditType.INITIAL : request.auditType());
        audit.setStage(request.stage() == null ? AuditStage.NOT_APPLICABLE : request.stage());
        audit.setStatus(AuditStatus.PLANNED);
        audit.setPlannedStartOn(request.plannedStartOn());
        audit.setPlannedEndOn(request.plannedEndOn());
        audit.setNotes(blankToNull(request.notes()));
        auditRepository.save(audit);
        auditLogService.record("AUDIT_CREATE", "Audit", audit.getId(), null, audit.getAuditNumber(), null, null);
        return toDetail(audit);
    }

    @Transactional
    public AuditResponse update(String id, UpdateAuditRequest request) {
        Audit audit = requirePlannable(id);
        if (request.name() != null && !request.name().isBlank()) {
            audit.setName(request.name().trim());
        }
        if (request.auditType() != null) {
            audit.setAuditType(request.auditType());
        }
        if (request.stage() != null) {
            audit.setStage(request.stage());
        }
        if (request.checklistId() != null) {
            AuditProgramme programme = programmeService.requireProgramme(audit.getProgrammeId());
            audit.setChecklistId(resolveChecklist(programme, request.checklistId().isBlank() ? null : request.checklistId()));
        }
        if (request.plannedStartOn() != null) {
            audit.setPlannedStartOn(request.plannedStartOn());
        }
        if (request.plannedEndOn() != null) {
            audit.setPlannedEndOn(request.plannedEndOn());
        }
        if (request.notes() != null) {
            audit.setNotes(blankToNull(request.notes()));
        }
        if (audit.getPlannedStartOn() != null && audit.getPlannedEndOn() != null
                && audit.getPlannedEndOn().isBefore(audit.getPlannedStartOn())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "plannedEndOn cannot be before plannedStartOn");
        }
        auditRepository.save(audit);
        return toDetail(audit);
    }

    @Transactional
    public AuditResponse schedule(String id) {
        Audit audit = requirePlannable(id);
        if (audit.getPlannedStartOn() == null || audit.getPlannedEndOn() == null) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Schedule requires planned start and end dates");
        }
        if (!assignmentRepository.existsByAuditIdAndAssignmentRoleAndDeletedAtIsNull(audit.getId(), AssignmentRole.LEAD)) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Schedule requires a lead auditor assignment");
        }
        audit.setStatus(AuditStatus.SCHEDULED);
        auditRepository.save(audit);
        auditLogService.record("AUDIT_SCHEDULE", "Audit", audit.getId(), "PLANNED", "SCHEDULED", null, null);
        return toDetail(audit);
    }

    @Transactional
    public AuditResponse cancel(String id) {
        Audit audit = requireAudit(id);
        if (audit.getStatus() == AuditStatus.COMPLETED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Completed audits cannot be cancelled");
        }
        audit.setStatus(AuditStatus.CANCELLED);
        auditRepository.save(audit);
        return toDetail(audit);
    }

    @Transactional
    public void delete(String id) {
        Audit audit = requireAudit(id);
        if (audit.getStatus() != AuditStatus.PLANNED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only planned audits can be deleted; cancel scheduled work instead");
        }
        Instant now = Instant.now();
        audit.setDeletedAt(now);
        auditRepository.save(audit);
        auditLogService.record("AUDIT_DELETE", "Audit", audit.getId(), audit.getAuditNumber(), null, null, null);
    }

    @Transactional(readOnly = true)
    public List<AuditSiteResponse> listSites(String auditId) {
        Audit audit = requireAudit(auditId);
        return auditSiteRepository
                .findByTenantIdAndAuditIdAndDeletedAtIsNull(audit.getTenantId(), audit.getId())
                .stream()
                .map(AuditSiteResponse::from)
                .toList();
    }

    @Transactional
    public AuditSiteResponse addSite(String auditId, AddAuditSiteRequest request) {
        Audit audit = requirePlannable(auditId);
        Site site = siteService.requireSite(request.siteId());
        if (!audit.getClientId().equals(site.getClientId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Site does not belong to the audit client");
        }
        if (auditSiteRepository.existsByAuditIdAndSiteIdAndDeletedAtIsNull(audit.getId(), site.getId())) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "Site is already in scope");
        }
        AuditSite link = new AuditSite();
        link.setTenantId(audit.getTenantId());
        link.setAuditId(audit.getId());
        link.setSiteId(site.getId());
        auditSiteRepository.save(link);
        return AuditSiteResponse.from(link);
    }

    @Transactional
    public void removeSite(String auditSiteId) {
        AuditSite link = auditSiteRepository.findByIdAndDeletedAtIsNull(auditSiteId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Audit site not found"));
        isolationService.assertCanAccessTenant(link.getTenantId());
        requirePlannable(link.getAuditId());
        link.setDeletedAt(Instant.now());
        auditSiteRepository.save(link);
    }

    public Audit requireAudit(String id) {
        Audit audit = auditRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Audit not found"));
        isolationService.assertCanAccessTenant(audit.getTenantId());
        return audit;
    }

    Audit requirePlannable(String id) {
        Audit audit = requireAudit(id);
        if (audit.getStatus() != AuditStatus.PLANNED && audit.getStatus() != AuditStatus.SCHEDULED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only planned or scheduled audits can be changed in this phase");
        }
        return audit;
    }

    private AuditResponse toDetail(Audit audit) {
        List<AuditSiteResponse> sites = auditSiteRepository
                .findByTenantIdAndAuditIdAndDeletedAtIsNull(audit.getTenantId(), audit.getId())
                .stream()
                .map(AuditSiteResponse::from)
                .toList();
        List<AssignmentResponse> assignments = assignmentRepository
                .findByTenantIdAndAuditIdAndDeletedAtIsNull(audit.getTenantId(), audit.getId())
                .stream()
                .map(AssignmentResponse::from)
                .toList();
        return AuditResponse.from(audit, sites, assignments);
    }

    private String resolveChecklist(AuditProgramme programme, String checklistId) {
        if (checklistId == null || checklistId.isBlank()) {
            return null;
        }
        Checklist checklist = checklistService.requireChecklist(checklistId);
        if (!programme.getSchemeId().equals(checklist.getSchemeId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Checklist does not belong to the programme scheme");
        }
        return checklist.getId();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
