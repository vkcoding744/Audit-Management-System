package com.auditplatform.finance.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateInvoiceRequest(
        @NotBlank String clientId,
        String quoteId,
        @Size(min = 3, max = 3) @Pattern(regexp = "[A-Z]{3}") String currency,
        LocalDate dueOn,
        String notes,
        @NotEmpty @Valid List<LineRequest> lines
) {
}
