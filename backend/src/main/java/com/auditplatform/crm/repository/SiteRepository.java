package com.auditplatform.crm.repository;

import com.auditplatform.crm.domain.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, String> {

    Optional<Site> findByIdAndDeletedAtIsNull(String id);

    List<Site> findByTenantIdAndClientIdAndDeletedAtIsNullOrderByNameAsc(String tenantId, String clientId);

    long countByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId);
}
