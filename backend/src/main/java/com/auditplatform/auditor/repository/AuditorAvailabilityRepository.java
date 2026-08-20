package com.auditplatform.auditor.repository;

import com.auditplatform.auditor.domain.AuditorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditorAvailabilityRepository extends JpaRepository<AuditorAvailability, String> {

    Optional<AuditorAvailability> findByIdAndDeletedAtIsNull(String id);

    List<AuditorAvailability> findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByStartOnAsc(String tenantId, String auditorId);
}
