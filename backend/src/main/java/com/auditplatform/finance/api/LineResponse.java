package com.auditplatform.finance.api;

import com.auditplatform.finance.domain.QuoteLine;

import java.math.BigDecimal;

public record LineResponse(
        String id,
        String description,
        BigDecimal quantity,
        BigDecimal unitAmount,
        BigDecimal lineAmount
) {
    public static LineResponse from(QuoteLine line) {
        return new LineResponse(
                line.getId(),
                line.getDescription(),
                line.getQuantity(),
                line.getUnitAmount(),
                line.getLineAmount()
        );
    }

    public static LineResponse from(com.auditplatform.finance.domain.InvoiceLine line) {
        return new LineResponse(
                line.getId(),
                line.getDescription(),
                line.getQuantity(),
                line.getUnitAmount(),
                line.getLineAmount()
        );
    }
}
