package com.auditplatform.finance.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.finance.api.CreateQuoteRequest;
import com.auditplatform.finance.api.InvoiceResponse;
import com.auditplatform.finance.api.QuoteResponse;
import com.auditplatform.finance.domain.QuoteStatus;
import com.auditplatform.finance.service.InvoiceService;
import com.auditplatform.finance.service.QuoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quotes")
@Tag(name = "Quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final InvoiceService invoiceService;

    public QuoteController(QuoteService quoteService, InvoiceService invoiceService) {
        this.quoteService = quoteService;
        this.invoiceService = invoiceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVOICE_VIEW')")
    public ApiResponse<PageResponse<QuoteResponse>> list(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) QuoteStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(quoteService.list(clientId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuoteResponse> create(@Valid @RequestBody CreateQuoteRequest request) {
        return ApiResponse.ok(quoteService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVOICE_VIEW')")
    public ApiResponse<QuoteResponse> get(@PathVariable String id) {
        return ApiResponse.ok(quoteService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    public ApiResponse<QuoteResponse> issue(@PathVariable String id) {
        return ApiResponse.ok(quoteService.issue(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    public ApiResponse<QuoteResponse> accept(@PathVariable String id) {
        return ApiResponse.ok(quoteService.accept(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/decline")
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    public ApiResponse<QuoteResponse> decline(@PathVariable String id) {
        return ApiResponse.ok(quoteService.decline(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/invoice")
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InvoiceResponse> invoice(@PathVariable String id) {
        return ApiResponse.ok(invoiceService.createFromQuote(id), MDC.get(CorrelationId.MDC_KEY));
    }
}
