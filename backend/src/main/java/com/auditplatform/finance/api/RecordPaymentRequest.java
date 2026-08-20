package com.auditplatform.finance.api;

import com.auditplatform.finance.domain.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordPaymentRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        LocalDate paidOn,
        PaymentMethod method,
        String reference,
        String notes
) {
}
