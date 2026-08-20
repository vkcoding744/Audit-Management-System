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
@Table(name = "capa_actions")
@Getter
@Setter
public class CapaAction extends TenantAwareEntity {

    @Column(name = "capa_number", nullable = false, length = 32)
    private String capaNumber;

    @Column(name = "finding_id", nullable = false, length = 36)
    private String findingId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_on", nullable = false)
    private LocalDate dueOn;

    @Column(name = "completed_on")
    private LocalDate completedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CapaStatus status = CapaStatus.OPEN;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
