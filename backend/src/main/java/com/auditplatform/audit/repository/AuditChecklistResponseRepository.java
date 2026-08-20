package com.auditplatform.audit.repository;

import com.auditplatform.audit.domain.AssessmentResult;
import com.auditplatform.audit.domain.AuditChecklistResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditChecklistResponseRepository extends JpaRepository<AuditChecklistResponse, String> {

    Optional<AuditChecklistResponse> findByIdAndDeletedAtIsNull(String id);

    List<AuditChecklistResponse> findByTenantIdAndAuditIdAndDeletedAtIsNullOrderBySortOrderAsc(String tenantId, String auditId);

    boolean existsByAuditIdAndDeletedAtIsNull(String auditId);

    long countByAuditIdAndRequiredIsTrueAndResultAndDeletedAtIsNull(
            String auditId,
            AssessmentResult result
    );
}
