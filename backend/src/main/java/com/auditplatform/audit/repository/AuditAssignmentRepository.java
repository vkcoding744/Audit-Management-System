package com.auditplatform.audit.repository;

import com.auditplatform.audit.domain.AssignmentRole;
import com.auditplatform.audit.domain.AuditAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditAssignmentRepository extends JpaRepository<AuditAssignment, String> {

    Optional<AuditAssignment> findByIdAndDeletedAtIsNull(String id);

    List<AuditAssignment> findByTenantIdAndAuditIdAndDeletedAtIsNull(String tenantId, String auditId);

    boolean existsByAuditIdAndAuditorIdAndDeletedAtIsNull(String auditId, String auditorId);

    boolean existsByAuditIdAndAssignmentRoleAndDeletedAtIsNull(String auditId, AssignmentRole assignmentRole);
}
