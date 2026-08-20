package com.auditplatform.finance.api;

import com.auditplatform.finance.domain.Payment;
import com.auditplatform.finance.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        String id,
        String tenantId,
        String paymentNumber,
        String invoiceId,
        BigDecimal amount,
        LocalDate paidOn,
        PaymentMethod method,
        String reference,
        String notes
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getTenantId(),
                payment.getPaymentNumber(),
                payment.getInvoiceId(),
                payment.getAmount(),
                payment.getPaidOn(),
                payment.getMethod(),
                payment.getReference(),
                payment.getNotes()
        );
    }
}
