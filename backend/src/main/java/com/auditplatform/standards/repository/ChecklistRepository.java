package com.auditplatform.standards.repository;

import com.auditplatform.standards.domain.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChecklistRepository extends JpaRepository<Checklist, String> {

    Optional<Checklist> findByIdAndDeletedAtIsNull(String id);

    List<Checklist> findByTenantIdAndSchemeIdAndDeletedAtIsNullOrderByNameAsc(String tenantId, String schemeId);

    boolean existsBySchemeIdAndNameAndVersionLabelAndDeletedAtIsNull(String schemeId, String name, String versionLabel);

    boolean existsByStandardIdAndDeletedAtIsNull(String standardId);

    boolean existsBySchemeIdAndDeletedAtIsNull(String schemeId);
}
