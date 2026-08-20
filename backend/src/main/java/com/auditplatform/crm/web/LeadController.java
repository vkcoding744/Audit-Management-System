package com.auditplatform.crm.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.crm.api.CreateLeadRequest;
import com.auditplatform.crm.api.LeadResponse;
import com.auditplatform.crm.api.LoseLeadRequest;
import com.auditplatform.crm.api.UpdateLeadRequest;
import com.auditplatform.crm.domain.LeadStatus;
import com.auditplatform.crm.service.LeadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leads")
@Tag(name = "Leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LEAD_VIEW')")
    public ApiResponse<PageResponse<LeadResponse>> list(
            @RequestParam(required = false) LeadStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(leadService.list(status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('LEAD_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LeadResponse> create(@Valid @RequestBody CreateLeadRequest request) {
        return ApiResponse.ok(leadService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAD_VIEW')")
    public ApiResponse<LeadResponse> get(@PathVariable String id) {
        return ApiResponse.ok(leadService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAD_UPDATE')")
    public ApiResponse<LeadResponse> update(@PathVariable String id, @Valid @RequestBody UpdateLeadRequest request) {
        return ApiResponse.ok(leadService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/qualify")
    @PreAuthorize("hasAuthority('LEAD_UPDATE')")
    public ApiResponse<LeadResponse> qualify(@PathVariable String id) {
        return ApiResponse.ok(leadService.qualify(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/lose")
    @PreAuthorize("hasAuthority('LEAD_UPDATE')")
    public ApiResponse<LeadResponse> lose(@PathVariable String id, @Valid @RequestBody LoseLeadRequest request) {
        return ApiResponse.ok(leadService.lose(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAuthority('LEAD_UPDATE') and hasAuthority('CLIENT_CREATE')")
    public ApiResponse<LeadResponse> convert(@PathVariable String id) {
        return ApiResponse.ok(leadService.convert(id), MDC.get(CorrelationId.MDC_KEY));
    }
}
