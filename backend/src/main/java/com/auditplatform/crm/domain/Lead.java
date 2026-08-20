package com.auditplatform.crm.domain;

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
@Table(name = "leads")
@Getter
@Setter
public class Lead extends TenantAwareEntity {

    @Column(name = "lead_number", nullable = false, length = 32)
    private String leadNumber;

    @Column(name = "organisation_name", nullable = false)
    private String organisationName;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 64)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 32)
    private LeadSource source = LeadSource.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private LeadStatus status = LeadStatus.OPEN;

    @Column(name = "converted_client_id", length = 36)
    private String convertedClientId;

    @Column(name = "converted_at")
    private Instant convertedAt;

    @Column(name = "lost_reason", columnDefinition = "TEXT")
    private String lostReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
