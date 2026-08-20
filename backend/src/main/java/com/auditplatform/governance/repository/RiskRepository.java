package com.auditplatform.governance.repository;

import com.auditplatform.governance.domain.Risk;
import com.auditplatform.governance.domain.RiskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskRepository extends JpaRepository<Risk, String> {

    Optional<Risk> findByIdAndDeletedAtIsNull(String id);

    Page<Risk> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Risk> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, RiskStatus status, Pageable pageable);
}
