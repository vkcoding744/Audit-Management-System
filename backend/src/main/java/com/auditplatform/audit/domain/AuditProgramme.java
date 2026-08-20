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
@Table(name = "audit_programmes")
@Getter
@Setter
public class AuditProgramme extends TenantAwareEntity {

    @Column(name = "programme_number", nullable = false, length = 32)
    private String programmeNumber;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "scheme_id", nullable = false, length = 36)
    private String schemeId;

    @Column(name = "standard_id", length = 36)
    private String standardId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ProgrammeStatus status = ProgrammeStatus.DRAFT;

    @Column(name = "cycle_start_on")
    private LocalDate cycleStartOn;

    @Column(name = "cycle_end_on")
    private LocalDate cycleEndOn;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
