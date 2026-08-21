package com.auditplatform.document.repository;

import com.auditplatform.document.domain.Document;
import com.auditplatform.document.domain.DocumentLinkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select d from Document d
            where d.tenantId = :tenantId and d.deletedAt is null
              and (
                lower(d.title) like lower(concat('%', :q, '%')) escape '\\'
                or lower(d.documentNumber) like lower(concat('%', :q, '%')) escape '\\'
                or lower(d.originalFilename) like lower(concat('%', :q, '%')) escape '\\'
              )
            """)
    Page<Document> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
