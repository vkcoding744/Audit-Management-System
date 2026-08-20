package com.auditplatform.document.repository;

import com.auditplatform.document.domain.Document;
import com.auditplatform.document.domain.DocumentLinkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, String> {

    Optional<Document> findByIdAndDeletedAtIsNull(String id);

    Page<Document> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Document> findByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId, Pageable pageable);

    Page<Document> findByTenantIdAndLinkedTypeAndDeletedAtIsNull(
            String tenantId,
            DocumentLinkType linkedType,
            Pageable pageable
    );

    Page<Document> findByTenantIdAndLinkedIdAndDeletedAtIsNull(String tenantId, String linkedId, Pageable pageable);

    long countByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId);
}
