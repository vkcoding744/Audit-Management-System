package com.auditplatform.finance.repository;

import com.auditplatform.finance.domain.Invoice;
import com.auditplatform.finance.domain.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    Optional<Invoice> findByIdAndDeletedAtIsNull(String id);

    Page<Invoice> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Invoice> findByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId, Pageable pageable);

    Page<Invoice> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, InvoiceStatus status, Pageable pageable);

    boolean existsByQuoteIdAndDeletedAtIsNull(String quoteId);

    long countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
            String tenantId,
            String clientId,
            Collection<InvoiceStatus> statuses
    );
}
