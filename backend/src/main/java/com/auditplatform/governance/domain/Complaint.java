package com.auditplatform.governance.domain;

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
@Table(name = "complaints")
@Getter
@Setter
public class Complaint extends TenantAwareEntity {

    @Column(name = "complaint_number", nullable = false, length = 32)
    private String complaintNumber;

    @Column(name = "client_id", length = 36)
    private String clientId;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 32)
    private ComplaintSource source = ComplaintSource.OTHER;

    @Column(name = "received_on", nullable = false)
    private LocalDate receivedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "closed_on")
    private LocalDate closedOn;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
