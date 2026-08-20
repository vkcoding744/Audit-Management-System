package com.auditplatform.standards.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.api.ClauseResponse;
import com.auditplatform.standards.api.CreateClauseRequest;
import com.auditplatform.standards.api.UpdateClauseRequest;
import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.domain.StandardClause;
import com.auditplatform.standards.repository.StandardClauseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ClauseService {

    private final StandardClauseRepository clauseRepository;
    private final StandardService standardService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public ClauseService(
            StandardClauseRepository clauseRepository,
            StandardService standardService,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.clauseRepository = clauseRepository;
        this.standardService = standardService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<ClauseResponse> listByStandard(String standardId) {
        Standard standard = standardService.requireStandard(standardId);
        return clauseRepository
                .findByTenantIdAndStandardIdAndDeletedAtIsNullOrderBySortOrderAscClauseCodeAsc(
                        standard.getTenantId(),
                        standard.getId()
                )
                .stream()
                .map(ClauseResponse::from)
                .toList();
    }

    @Transactional
    public ClauseResponse create(String standardId, CreateClauseRequest request) {
        Standard standard = standardService.requireDraft(standardId);
        String code = request.clauseCode().trim();
        if (clauseRepository.existsByStandardIdAndClauseCodeAndDeletedAtIsNull(standard.getId(), code)) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "A clause with this code already exists on the standard");
        }
        StandardClause clause = new StandardClause();
        clause.setTenantId(standard.getTenantId());
        clause.setStandardId(standard.getId());
        clause.setParentId(resolveParent(standard.getId(), request.parentId()));
        clause.setClauseCode(code);
        clause.setTitle(request.title().trim());
        clause.setRequirementText(blankToNull(request.requirementText()));
        clause.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        clauseRepository.save(clause);
        auditLogService.record("CLAUSE_CREATE", "StandardClause", clause.getId(), null, clause.getClauseCode(), null, null);
        return ClauseResponse.from(clause);
    }

    @Transactional
    public ClauseResponse update(String clauseId, UpdateClauseRequest request) {
        StandardClause clause = requireClause(clauseId);
        standardService.requireDraft(clause.getStandardId());
        if (request.clauseCode() != null && !request.clauseCode().isBlank()) {
            String code = request.clauseCode().trim();
            if (!code.equals(clause.getClauseCode())
                    && clauseRepository.existsByStandardIdAndClauseCodeAndDeletedAtIsNull(clause.getStandardId(), code)) {
                throw new ApiException(ErrorCode.SYS_CONFLICT, "A clause with this code already exists on the standard");
            }
            clause.setClauseCode(code);
        }
        if (request.title() != null && !request.title().isBlank()) {
            clause.setTitle(request.title().trim());
        }
        if (request.requirementText() != null) {
            clause.setRequirementText(blankToNull(request.requirementText()));
        }
        if (request.sortOrder() != null) {
            clause.setSortOrder(request.sortOrder());
        }
        if (request.parentId() != null) {
            clause.setParentId(resolveParent(clause.getStandardId(), request.parentId().isBlank() ? null : request.parentId()));
        }
        clauseRepository.save(clause);
        return ClauseResponse.from(clause);
    }

    @Transactional
    public void delete(String clauseId) {
        StandardClause clause = requireClause(clauseId);
        standardService.requireDraft(clause.getStandardId());
        Instant now = Instant.now();
        softDeleteTree(clause, now);
        auditLogService.record("CLAUSE_DELETE", "StandardClause", clause.getId(), clause.getClauseCode(), null, null, null);
    }

    StandardClause requireClause(String id) {
        StandardClause clause = clauseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Clause not found"));
        isolationService.assertCanAccessTenant(clause.getTenantId());
        return clause;
    }

    private void softDeleteTree(StandardClause clause, Instant now) {
        clauseRepository.findByTenantIdAndParentIdAndDeletedAtIsNull(clause.getTenantId(), clause.getId())
                .forEach(child -> softDeleteTree(child, now));
        clause.setDeletedAt(now);
        clauseRepository.save(clause);
    }

    private String resolveParent(String standardId, String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return null;
        }
        StandardClause parent = requireClause(parentId);
        if (!standardId.equals(parent.getStandardId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Parent clause does not belong to this standard");
        }
        return parent.getId();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
