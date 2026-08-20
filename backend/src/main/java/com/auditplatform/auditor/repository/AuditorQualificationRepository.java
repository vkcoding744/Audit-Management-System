package com.auditplatform.auditor.repository;

import com.auditplatform.auditor.domain.AuditorQualification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditorQualificationRepository extends JpaRepository<AuditorQualification, String> {

    Optional<AuditorQualification> findByIdAndDeletedAtIsNull(String id);

    List<AuditorQualification> findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByTitleAsc(String tenantId, String auditorId);
}
