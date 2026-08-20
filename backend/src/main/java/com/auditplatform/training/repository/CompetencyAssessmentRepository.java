package com.auditplatform.training.repository;

import com.auditplatform.training.domain.AssessmentStatus;
import com.auditplatform.training.domain.CompetencyAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetencyAssessmentRepository extends JpaRepository<CompetencyAssessment, String> {

    Optional<CompetencyAssessment> findByIdAndDeletedAtIsNull(String id);

    Page<CompetencyAssessment> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<CompetencyAssessment> findByTenantIdAndAuditorIdAndDeletedAtIsNull(String tenantId, String auditorId, Pageable pageable);

    Page<CompetencyAssessment> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, AssessmentStatus status, Pageable pageable);

    Page<CompetencyAssessment> findByTenantIdAndAuditorIdAndStatusAndDeletedAtIsNull(
            String tenantId,
            String auditorId,
            AssessmentStatus status,
            Pageable pageable
    );
}
