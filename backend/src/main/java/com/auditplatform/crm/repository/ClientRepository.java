package com.auditplatform.crm.repository;

import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.domain.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {

    Optional<Client> findByIdAndDeletedAtIsNull(String id);

    Page<Client> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Client> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, ClientStatus status, Pageable pageable);

    @Query("""
            select c from Client c
            where c.tenantId = :tenantId and c.deletedAt is null
              and (
                lower(c.legalName) like lower(concat('%', :q, '%'))
                or lower(coalesce(c.tradingName, '')) like lower(concat('%', :q, '%'))
                or lower(c.clientNumber) like lower(concat('%', :q, '%'))
              )
            """)
    Page<Client> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);

    long countByTenantIdAndDeletedAtIsNull(String tenantId);
}
