package com.auditplatform.standards.repository;

import com.auditplatform.standards.domain.StandardClause;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StandardClauseRepository extends JpaRepository<StandardClause, String> {

    Optional<StandardClause> findByIdAndDeletedAtIsNull(String id);

    List<StandardClause> findByTenantIdAndStandardIdAndDeletedAtIsNullOrderBySortOrderAscClauseCodeAsc(
            String tenantId,
            String standardId
    );

    boolean existsByStandardIdAndClauseCodeAndDeletedAtIsNull(String standardId, String clauseCode);

    List<StandardClause> findByTenantIdAndParentIdAndDeletedAtIsNull(String tenantId, String parentId);
}
