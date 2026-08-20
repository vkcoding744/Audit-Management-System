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

@Entity
@Table(name = "audit_assignments")
@Getter
@Setter
public class AuditAssignment extends TenantAwareEntity {

    @Column(name = "audit_id", nullable = false, length = 36)
    private String auditId;

    @Column(name = "auditor_id", nullable = false, length = 36)
    private String auditorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_role", nullable = false, length = 32)
    private AssignmentRole assignmentRole = AssignmentRole.TEAM;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
