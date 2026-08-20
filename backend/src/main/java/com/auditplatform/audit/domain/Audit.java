package com.auditplatform.audit.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "audits")
@Getter
@Setter
public class Audit extends TenantAwareEntity {

    @Column(name = "audit_number", nullable = false, length = 32)
    private String auditNumber;

    @Column(name = "programme_id", nullable = false, length = 36)
    private String programmeId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "scheme_id", nullable = false, length = 36)
    private String schemeId;

    @Column(name = "standard_id", length = 36)
    private String standardId;

    @Column(name = "checklist_id", length = 36)
    private String checklistId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_type", nullable = false, length = 32)
    private AuditType auditType = AuditType.INITIAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 32)
    private AuditStage stage = AuditStage.NOT_APPLICABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AuditStatus status = AuditStatus.PLANNED;

    @Column(name = "planned_start_on")
    private LocalDate plannedStartOn;

    @Column(name = "planned_end_on")
    private LocalDate plannedEndOn;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
