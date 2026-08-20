package com.auditplatform.standards.domain;

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
@Table(name = "checklist_items")
@Getter
@Setter
public class ChecklistItem extends TenantAwareEntity {

    @Column(name = "checklist_id", nullable = false, length = 36)
    private String checklistId;

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

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
