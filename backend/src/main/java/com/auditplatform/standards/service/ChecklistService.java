package com.auditplatform.standards.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.api.ChecklistItemResponse;
import com.auditplatform.standards.api.ChecklistResponse;
import com.auditplatform.standards.api.CreateChecklistItemRequest;
import com.auditplatform.standards.api.CreateChecklistRequest;
import com.auditplatform.standards.api.UpdateChecklistItemRequest;
import com.auditplatform.standards.api.UpdateChecklistRequest;
import com.auditplatform.standards.domain.Checklist;
import com.auditplatform.standards.domain.ChecklistItem;
import com.auditplatform.standards.domain.ChecklistItemType;
import com.auditplatform.standards.domain.ChecklistStatus;
import com.auditplatform.standards.domain.Scheme;
import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.domain.StandardClause;
import com.auditplatform.standards.repository.ChecklistItemRepository;
import com.auditplatform.standards.repository.ChecklistRepository;
import com.auditplatform.standards.repository.SchemeStandardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository itemRepository;
    private final SchemeStandardRepository schemeStandardRepository;
    private final SchemeService schemeService;
    private final StandardService standardService;
    private final ClauseService clauseService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public ChecklistService(
            ChecklistRepository checklistRepository,
            ChecklistItemRepository itemRepository,
            SchemeStandardRepository schemeStandardRepository,
            SchemeService schemeService,
            StandardService standardService,
            ClauseService clauseService,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.checklistRepository = checklistRepository;
        this.itemRepository = itemRepository;
        this.schemeStandardRepository = schemeStandardRepository;
        this.schemeService = schemeService;
        this.standardService = standardService;
        this.clauseService = clauseService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<ChecklistResponse> listByScheme(String schemeId) {
        Scheme scheme = schemeService.requireScheme(schemeId);
        return checklistRepository.findByTenantIdAndSchemeIdAndDeletedAtIsNullOrderByNameAsc(scheme.getTenantId(), scheme.getId())
                .stream()
                .map(ChecklistResponse::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChecklistResponse get(String id) {
        Checklist checklist = requireChecklist(id);
        return ChecklistResponse.from(checklist, items(checklist));
    }

    @Transactional
    public ChecklistResponse create(String schemeId, CreateChecklistRequest request) {
        Scheme scheme = schemeService.requireScheme(schemeId);
        String name = request.name().trim();
        String versionLabel = request.versionLabel().trim();
        if (checklistRepository.existsBySchemeIdAndNameAndVersionLabelAndDeletedAtIsNull(scheme.getId(), name, versionLabel)) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "A checklist with this name and version already exists on the scheme");
        }
        Checklist checklist = new Checklist();
        checklist.setTenantId(scheme.getTenantId());
        checklist.setSchemeId(scheme.getId());
        checklist.setName(name);
        checklist.setVersionLabel(versionLabel);
        checklist.setStandardId(resolveStandard(scheme, request.standardId()));
        if (request.status() != null && request.status() != ChecklistStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "New checklists must be created as DRAFT");
        }
        checklist.setStatus(ChecklistStatus.DRAFT);
        checklist.setNotes(blankToNull(request.notes()));
        checklistRepository.save(checklist);
        auditLogService.record("CHECKLIST_CREATE", "Checklist", checklist.getId(), null, checklist.getName(), null, null);
        return ChecklistResponse.from(checklist, List.of());
    }

    @Transactional
    public ChecklistResponse update(String id, UpdateChecklistRequest request) {
        Checklist checklist = requireDraft(id);
        if (request.name() != null && !request.name().isBlank()) {
            checklist.setName(request.name().trim());
        }
        if (request.versionLabel() != null && !request.versionLabel().isBlank()) {
            checklist.setVersionLabel(request.versionLabel().trim());
        }
        if (request.standardId() != null) {
            Scheme scheme = schemeService.requireScheme(checklist.getSchemeId());
            checklist.setStandardId(resolveStandard(scheme, request.standardId().isBlank() ? null : request.standardId()));
        }
        if (request.notes() != null) {
            checklist.setNotes(blankToNull(request.notes()));
        }
        if (request.status() != null && request.status() != checklist.getStatus()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Use activate or archive to change status");
        }
        checklistRepository.save(checklist);
        return ChecklistResponse.from(checklist, items(checklist));
    }

    @Transactional
    public ChecklistResponse activate(String id) {
        Checklist checklist = requireChecklist(id);
        if (checklist.getStatus() != ChecklistStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft checklists can be activated");
        }
        if (itemRepository.countByTenantIdAndChecklistIdAndDeletedAtIsNull(checklist.getTenantId(), checklist.getId()) == 0) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Activate requires at least one checklist item");
        }
        checklist.setStatus(ChecklistStatus.ACTIVE);
        checklistRepository.save(checklist);
        auditLogService.record("CHECKLIST_ACTIVATE", "Checklist", checklist.getId(), "DRAFT", "ACTIVE", null, null);
        return ChecklistResponse.from(checklist, items(checklist));
    }

    @Transactional
    public ChecklistResponse archive(String id) {
        Checklist checklist = requireChecklist(id);
        if (checklist.getStatus() != ChecklistStatus.ACTIVE) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only active checklists can be archived");
        }
        checklist.setStatus(ChecklistStatus.ARCHIVED);
        checklistRepository.save(checklist);
        auditLogService.record("CHECKLIST_ARCHIVE", "Checklist", checklist.getId(), "ACTIVE", "ARCHIVED", null, null);
        return ChecklistResponse.from(checklist, items(checklist));
    }

    @Transactional
    public void delete(String id) {
        Checklist checklist = requireChecklist(id);
        Instant now = Instant.now();
        checklist.setDeletedAt(now);
        checklistRepository.save(checklist);
        itemRepository.findByTenantIdAndChecklistIdAndDeletedAtIsNullOrderBySortOrderAsc(checklist.getTenantId(), checklist.getId())
                .forEach(item -> {
                    item.setDeletedAt(now);
                    itemRepository.save(item);
                });
        auditLogService.record("CHECKLIST_DELETE", "Checklist", checklist.getId(), checklist.getName(), null, null, null);
    }

    @Transactional
    public ChecklistItemResponse addItem(String checklistId, CreateChecklistItemRequest request) {
        Checklist checklist = requireDraft(checklistId);
        ChecklistItem item = new ChecklistItem();
        item.setTenantId(checklist.getTenantId());
        item.setChecklistId(checklist.getId());
        item.setClauseId(resolveClause(checklist, request.clauseId()));
        item.setTitle(request.title().trim());
        item.setGuidance(blankToNull(request.guidance()));
        item.setItemType(request.itemType() == null ? ChecklistItemType.QUESTION : request.itemType());
        item.setRequired(request.required() == null || request.required());
        item.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        itemRepository.save(item);
        auditLogService.record("CHECKLIST_ITEM_CREATE", "ChecklistItem", item.getId(), null, item.getTitle(), null, null);
        return ChecklistItemResponse.from(item);
    }

    @Transactional
    public ChecklistItemResponse updateItem(String itemId, UpdateChecklistItemRequest request) {
        ChecklistItem item = requireItem(itemId);
        requireDraft(item.getChecklistId());
        if (request.title() != null && !request.title().isBlank()) {
            item.setTitle(request.title().trim());
        }
        if (request.guidance() != null) {
            item.setGuidance(blankToNull(request.guidance()));
        }
        if (request.itemType() != null) {
            item.setItemType(request.itemType());
        }
        if (request.required() != null) {
            item.setRequired(request.required());
        }
        if (request.sortOrder() != null) {
            item.setSortOrder(request.sortOrder());
        }
        if (request.clauseId() != null) {
            Checklist checklist = requireChecklist(item.getChecklistId());
            item.setClauseId(resolveClause(checklist, request.clauseId().isBlank() ? null : request.clauseId()));
        }
        itemRepository.save(item);
        return ChecklistItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(String itemId) {
        ChecklistItem item = requireItem(itemId);
        requireDraft(item.getChecklistId());
        item.setDeletedAt(Instant.now());
        itemRepository.save(item);
    }

    private List<ChecklistItemResponse> items(Checklist checklist) {
        return itemRepository
                .findByTenantIdAndChecklistIdAndDeletedAtIsNullOrderBySortOrderAsc(checklist.getTenantId(), checklist.getId())
                .stream()
                .map(ChecklistItemResponse::from)
                .toList();
    }

    private String resolveStandard(Scheme scheme, String standardId) {
        if (standardId == null || standardId.isBlank()) {
            return null;
        }
        Standard standard = standardService.requireStandard(standardId);
        if (!schemeStandardRepository.existsBySchemeIdAndStandardIdAndDeletedAtIsNull(scheme.getId(), standard.getId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Standard is not linked to this scheme");
        }
        return standard.getId();
    }

    private String resolveClause(Checklist checklist, String clauseId) {
        if (clauseId == null || clauseId.isBlank()) {
            return null;
        }
        StandardClause clause = clauseService.requireClause(clauseId);
        if (checklist.getStandardId() != null && !checklist.getStandardId().equals(clause.getStandardId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Clause does not belong to the checklist standard");
        }
        return clause.getId();
    }

    private Checklist requireDraft(String id) {
        Checklist checklist = requireChecklist(id);
        if (checklist.getStatus() != ChecklistStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Checklist items can only be edited while DRAFT");
        }
        return checklist;
    }

    Checklist requireChecklist(String id) {
        Checklist checklist = checklistRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Checklist not found"));
        isolationService.assertCanAccessTenant(checklist.getTenantId());
        return checklist;
    }

    private ChecklistItem requireItem(String id) {
        ChecklistItem item = itemRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Checklist item not found"));
        isolationService.assertCanAccessTenant(item.getTenantId());
        return item;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
