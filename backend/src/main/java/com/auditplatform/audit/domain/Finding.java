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
@Table(name = "findings")
@Getter
@Setter
public class Finding extends TenantAwareEntity {

    @Column(name = "finding_number", nullable = false, length = 32)
    private String findingNumber;

    @Column(name = "audit_id", nullable = false, length = 36)
    private String auditId;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "site_id", length = 36)
    private String siteId;

    @Column(name = "response_id", length = 36)
    private String responseId;

    @Column(name = "clause_id", length = 36)
    private String clauseId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 32)
    private FindingSeverity severity = FindingSeverity.MINOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private FindingStatus status = FindingStatus.OPEN;

    @Column(name = "closed_on")
    private LocalDate closedOn;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
