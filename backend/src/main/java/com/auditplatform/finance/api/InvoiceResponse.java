package com.auditplatform.finance.api;

import com.auditplatform.finance.domain.Invoice;
import com.auditplatform.finance.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(
        String id,
        String tenantId,
        String invoiceNumber,
        String clientId,
        String quoteId,
        String currency,
        InvoiceStatus status,
        LocalDate issuedOn,
        LocalDate dueOn,
        boolean overdue,
        BigDecimal subtotal,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal amountDue,
        String notes,
        List<LineResponse> lines,
        List<PaymentResponse> payments
) {
    public static InvoiceResponse from(
            Invoice invoice,
            boolean overdue,
            BigDecimal amountDue,
            List<LineResponse> lines,
            List<PaymentResponse> payments
    ) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getTenantId(),
                invoice.getInvoiceNumber(),
                invoice.getClientId(),
                invoice.getQuoteId(),
                invoice.getCurrency(),
                invoice.getStatus(),
                invoice.getIssuedOn(),
                invoice.getDueOn(),
                overdue,
                invoice.getSubtotal(),
                invoice.getTotalAmount(),
                invoice.getAmountPaid(),
                amountDue,
                invoice.getNotes(),
                lines,
                payments
        );
    }
}
