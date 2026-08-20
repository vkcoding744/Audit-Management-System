package com.auditplatform.finance.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.finance.api.CreateInvoiceRequest;
import com.auditplatform.finance.api.InvoiceResponse;
import com.auditplatform.finance.api.PaymentResponse;
import com.auditplatform.finance.api.RecordPaymentRequest;
import com.auditplatform.finance.domain.InvoiceStatus;
import com.auditplatform.finance.service.InvoiceService;
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
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVOICE_VIEW')")
    public ApiResponse<PageResponse<InvoiceResponse>> list(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) InvoiceStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(invoiceService.list(clientId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request) {
        return ApiResponse.ok(invoiceService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVOICE_VIEW')")
    public ApiResponse<InvoiceResponse> get(@PathVariable String id) {
        return ApiResponse.ok(invoiceService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    public ApiResponse<InvoiceResponse> issue(@PathVariable String id) {
        return ApiResponse.ok(invoiceService.issue(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAuthority('INVOICE_CREATE')")
    public ApiResponse<InvoiceResponse> voidInvoice(@PathVariable String id) {
        return ApiResponse.ok(invoiceService.voidInvoice(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAuthority('PAYMENT_RECORD')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponse> recordPayment(
            @PathVariable String id,
            @Valid @RequestBody RecordPaymentRequest request
    ) {
        return ApiResponse.ok(invoiceService.recordPayment(id, request), MDC.get(CorrelationId.MDC_KEY));
    }
}
