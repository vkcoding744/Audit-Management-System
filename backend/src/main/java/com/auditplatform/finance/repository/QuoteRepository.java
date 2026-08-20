package com.auditplatform.finance.repository;

import com.auditplatform.finance.domain.Quote;
import com.auditplatform.finance.domain.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, String> {

    Optional<Quote> findByIdAndDeletedAtIsNull(String id);

    Page<Quote> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Quote> findByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId, Pageable pageable);

    Page<Quote> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, QuoteStatus status, Pageable pageable);
}
