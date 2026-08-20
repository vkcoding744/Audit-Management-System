package com.auditplatform.standards.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "standard_clauses")
@Getter
@Setter
public class StandardClause extends TenantAwareEntity {

    @Column(name = "standard_id", nullable = false, length = 36)
    private String standardId;

    @Column(name = "parent_id", length = 36)
    private String parentId;

    @Column(name = "clause_code", nullable = false, length = 64)
    private String clauseCode;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "requirement_text", columnDefinition = "TEXT")
    private String requirementText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
