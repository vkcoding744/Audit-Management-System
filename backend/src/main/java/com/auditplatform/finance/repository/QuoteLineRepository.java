package com.auditplatform.finance.repository;

import com.auditplatform.finance.domain.QuoteLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteLineRepository extends JpaRepository<QuoteLine, String> {

    List<QuoteLine> findByTenantIdAndQuoteIdAndDeletedAtIsNullOrderByCreatedAtAsc(String tenantId, String quoteId);
}
