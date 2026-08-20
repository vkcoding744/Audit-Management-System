package com.auditplatform.finance.api;

import com.auditplatform.finance.domain.Quote;
import com.auditplatform.finance.domain.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record QuoteResponse(
        String id,
        String tenantId,
        String quoteNumber,
        String clientId,
        String currency,
        QuoteStatus status,
        LocalDate validUntil,
        boolean expired,
        BigDecimal subtotal,
        BigDecimal totalAmount,
        String notes,
        List<LineResponse> lines
) {
    public static QuoteResponse from(Quote quote, boolean expired, List<LineResponse> lines) {
        return new QuoteResponse(
                quote.getId(),
                quote.getTenantId(),
                quote.getQuoteNumber(),
                quote.getClientId(),
                quote.getCurrency(),
                quote.getStatus(),
                quote.getValidUntil(),
                expired,
                quote.getSubtotal(),
                quote.getTotalAmount(),
                quote.getNotes(),
                lines
        );
    }
}
