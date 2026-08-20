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
@Table(name = "checklists")
@Getter
@Setter
public class Checklist extends TenantAwareEntity {

    @Column(name = "scheme_id", nullable = false, length = 36)
    private String schemeId;

    @Column(name = "standard_id", length = 36)
    private String standardId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version_label", nullable = false, length = 32)
    private String versionLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ChecklistStatus status = ChecklistStatus.DRAFT;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
