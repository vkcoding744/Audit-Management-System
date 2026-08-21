package com.auditplatform.governance.repository;

import com.auditplatform.governance.domain.Appeal;
import com.auditplatform.governance.domain.AppealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface AppealRepository extends JpaRepository<Appeal, String> {

    Optional<Appeal> findByIdAndDeletedAtIsNull(String id);

    Page<Appeal> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Appeal> findByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId, Pageable pageable);

    Page<Appeal> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, AppealStatus status, Pageable pageable);

    long countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
            String tenantId,
            String clientId,
            Collection<AppealStatus> statuses
    );

    long countByTenantIdAndStatusInAndDeletedAtIsNull(String tenantId, Collection<AppealStatus> statuses);
}
