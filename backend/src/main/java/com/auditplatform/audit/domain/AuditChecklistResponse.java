package com.auditplatform.audit.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import com.auditplatform.standards.domain.ChecklistItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_checklist_responses")
@Getter
@Setter
public class AuditChecklistResponse extends TenantAwareEntity {

    @Column(name = "audit_id", nullable = false, length = 36)
    private String auditId;

    @Column(name = "checklist_item_id", nullable = false, length = 36)
    private String checklistItemId;

    @Column(name = "clause_id", length = 36)
    private String clauseId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "guidance", columnDefinition = "TEXT")
    private String guidance;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 32)
    private ChecklistItemType itemType = ChecklistItemType.QUESTION;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 32)
    private AssessmentResult result = AssessmentResult.NOT_ASSESSED;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "assessed_by", length = 36)
    private String assessedBy;

    @Column(name = "assessed_at")
    private Instant assessedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
