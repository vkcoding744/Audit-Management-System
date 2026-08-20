package com.auditplatform.audit.service;

import com.auditplatform.audit.api.AssignmentResponse;
import com.auditplatform.audit.api.CreateAssignmentRequest;
import com.auditplatform.audit.domain.AssignmentRole;
import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditAssignment;
import com.auditplatform.audit.repository.AuditAssignmentRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.api.EligibilityResponse;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.service.AuditorEligibilityService;
import com.auditplatform.auditor.service.AuditorService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AssignmentService {

    private final AuditAssignmentRepository assignmentRepository;
    private final AuditService auditService;
    private final AuditorService auditorService;
    private final AuditorEligibilityService eligibilityService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public AssignmentService(
            AuditAssignmentRepository assignmentRepository,
            AuditService auditService,
            AuditorService auditorService,
            AuditorEligibilityService eligibilityService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.assignmentRepository = assignmentRepository;
        this.auditService = auditService;
        this.auditorService = auditorService;
        this.eligibilityService = eligibilityService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> list(String auditId) {
        Audit audit = auditService.requireAudit(auditId);
        return assignmentRepository
                .findByTenantIdAndAuditIdAndDeletedAtIsNull(audit.getTenantId(), audit.getId())
                .stream()
                .map(AssignmentResponse::from)
                .toList();
    }

    @Transactional
    public AssignmentResponse assign(String auditId, CreateAssignmentRequest request) {
        Audit audit = auditService.requirePlannable(auditId);
        Auditor auditor = auditorService.requireAuditor(request.auditorId());
        if (!audit.getTenantId().equals(auditor.getTenantId())) {
            throw new ApiException(ErrorCode.AUTH_TENANT_MISMATCH, "Auditor does not belong to this tenant");
        }
        if (assignmentRepository.existsByAuditIdAndAuditorIdAndDeletedAtIsNull(audit.getId(), auditor.getId())) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "Auditor is already assigned to this audit");
        }
        AssignmentRole role = request.assignmentRole() == null ? AssignmentRole.TEAM : request.assignmentRole();
        if (role == AssignmentRole.LEAD
                && assignmentRepository.existsByAuditIdAndAssignmentRoleAndDeletedAtIsNull(audit.getId(), AssignmentRole.LEAD)) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "This audit already has a lead auditor");
        }
        LocalDate on = audit.getPlannedStartOn() != null
                ? audit.getPlannedStartOn()
                : LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        EligibilityResponse eligibility = eligibilityService.evaluate(
                auditor.getId() != null ? auditor.getId() : request.auditorId(),
                audit.getStandardId(),
                audit.getSchemeId(),
                on
        );
        if (!eligibility.eligible()) {
            throw new ApiException(
                    ErrorCode.SYS_VALIDATION,
                    "Auditor is not eligible for assignment: " + String.join(", ", eligibility.reasons())
            );
        }
        AuditAssignment assignment = new AuditAssignment();
        assignment.setTenantId(audit.getTenantId());
        assignment.setAuditId(audit.getId());
        assignment.setAuditorId(auditor.getId() != null ? auditor.getId() : request.auditorId());
        assignment.setAssignmentRole(role);
        assignmentRepository.save(assignment);
        auditLogService.record("AUDIT_ASSIGN", "AuditAssignment", assignment.getId(), null, role.name(), null, null);
        return AssignmentResponse.from(assignment);
    }

    @Transactional
    public void unassign(String assignmentId) {
        AuditAssignment assignment = assignmentRepository.findByIdAndDeletedAtIsNull(assignmentId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Assignment not found"));
        isolationService.assertCanAccessTenant(assignment.getTenantId());
        auditService.requirePlannable(assignment.getAuditId());
        assignment.setDeletedAt(Instant.now());
        assignmentRepository.save(assignment);
    }
}
