package com.auditplatform.governance.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.governance.api.CreateRiskRequest;
import com.auditplatform.governance.api.NotesRequest;
import com.auditplatform.governance.api.RiskResponse;
import com.auditplatform.governance.api.UpdateRiskRequest;
import com.auditplatform.governance.domain.RiskStatus;
import com.auditplatform.governance.service.RiskService;
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
@RequestMapping("/api/v1/risks")
@Tag(name = "Risks")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ApiResponse<PageResponse<RiskResponse>> list(
            @RequestParam(required = false) RiskStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(riskService.list(status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RISK_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RiskResponse> create(@Valid @RequestBody CreateRiskRequest request) {
        return ApiResponse.ok(riskService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ApiResponse<RiskResponse> get(@PathVariable String id) {
        return ApiResponse.ok(riskService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('RISK_UPDATE')")
    public ApiResponse<RiskResponse> update(@PathVariable String id, @Valid @RequestBody UpdateRiskRequest request) {
        return ApiResponse.ok(riskService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/mitigate")
    @PreAuthorize("hasAuthority('RISK_UPDATE')")
    public ApiResponse<RiskResponse> startMitigation(
            @PathVariable String id,
            @RequestBody(required = false) NotesRequest request
    ) {
        return ApiResponse.ok(riskService.startMitigation(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('RISK_UPDATE')")
    public ApiResponse<RiskResponse> close(
            @PathVariable String id,
            @RequestBody(required = false) NotesRequest request
    ) {
        return ApiResponse.ok(riskService.close(id, request), MDC.get(CorrelationId.MDC_KEY));
    }
}
