package com.auditplatform.finance.repository;

import com.auditplatform.finance.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findByTenantIdAndInvoiceIdAndDeletedAtIsNullOrderByPaidOnAsc(String tenantId, String invoiceId);
}
