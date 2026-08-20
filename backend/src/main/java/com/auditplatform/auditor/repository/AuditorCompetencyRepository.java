package com.auditplatform.auditor.repository;

import com.auditplatform.auditor.domain.AuditorCompetency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditorCompetencyRepository extends JpaRepository<AuditorCompetency, String> {

    Optional<AuditorCompetency> findByIdAndDeletedAtIsNull(String id);

    List<AuditorCompetency> findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByValidFromDesc(String tenantId, String auditorId);
}
