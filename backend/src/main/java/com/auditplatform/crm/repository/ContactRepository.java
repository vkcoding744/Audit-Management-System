package com.auditplatform.crm.repository;

import com.auditplatform.crm.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, String> {

    Optional<Contact> findByIdAndDeletedAtIsNull(String id);

    List<Contact> findByTenantIdAndClientIdAndDeletedAtIsNullOrderByLastNameAsc(String tenantId, String clientId);

    long countByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Contact c set c.primaryContact = false where c.tenantId = :tenantId and c.clientId = :clientId and c.deletedAt is null and c.id <> :exceptId")
    int clearPrimaryExcept(@Param("tenantId") String tenantId, @Param("clientId") String clientId, @Param("exceptId") String exceptId);
}
