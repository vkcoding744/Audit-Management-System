package com.auditplatform.tenant.repository;

import com.auditplatform.tenant.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, String> {

    Optional<Tenant> findByCodeAndDeletedAtIsNull(String code);

    long countByDeletedAtIsNull();
}
