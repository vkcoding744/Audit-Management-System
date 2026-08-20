package com.auditplatform.finance.repository;

import com.auditplatform.finance.domain.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, String> {

    List<InvoiceLine> findByTenantIdAndInvoiceIdAndDeletedAtIsNullOrderByCreatedAtAsc(String tenantId, String invoiceId);
}
