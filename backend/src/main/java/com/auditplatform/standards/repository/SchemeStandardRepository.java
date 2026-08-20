package com.auditplatform.standards.repository;

import com.auditplatform.standards.domain.SchemeStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchemeStandardRepository extends JpaRepository<SchemeStandard, String> {

    List<SchemeStandard> findByTenantIdAndSchemeIdAndDeletedAtIsNull(String tenantId, String schemeId);

    Optional<SchemeStandard> findBySchemeIdAndStandardIdAndDeletedAtIsNull(String schemeId, String standardId);

    boolean existsBySchemeIdAndStandardIdAndDeletedAtIsNull(String schemeId, String standardId);

    boolean existsByStandardIdAndDeletedAtIsNull(String standardId);
}
