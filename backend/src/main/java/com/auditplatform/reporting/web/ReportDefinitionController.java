package com.auditplatform.reporting.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.reporting.api.CreateReportRequest;
import com.auditplatform.reporting.api.ReportDefinitionResponse;
import com.auditplatform.reporting.api.ReportExportResponse;
import com.auditplatform.reporting.api.UpdateReportRequest;
import com.auditplatform.reporting.domain.ReportDataset;
import com.auditplatform.reporting.domain.ReportDefinitionStatus;
import com.auditplatform.reporting.service.ReportDefinitionService;
import com.auditplatform.reporting.service.ReportExportService;
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
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports")
public class ReportDefinitionController {

    private final ReportDefinitionService definitionService;
    private final ReportExportService exportService;

    public ReportDefinitionController(ReportDefinitionService definitionService, ReportExportService exportService) {
        this.definitionService = definitionService;
        this.exportService = exportService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ApiResponse<PageResponse<ReportDefinitionResponse>> list(
            @RequestParam(required = false) ReportDefinitionStatus status,
            @RequestParam(required = false) ReportDataset dataset,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(definitionService.list(status, dataset, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportDefinitionResponse> create(@Valid @RequestBody CreateReportRequest request) {
        return ApiResponse.ok(definitionService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ApiResponse<ReportDefinitionResponse> get(@PathVariable String id) {
        return ApiResponse.ok(definitionService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ApiResponse<ReportDefinitionResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateReportRequest request
    ) {
        return ApiResponse.ok(definitionService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ApiResponse<ReportDefinitionResponse> publish(@PathVariable String id) {
        return ApiResponse.ok(definitionService.publish(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ApiResponse<ReportDefinitionResponse> archive(@PathVariable String id) {
        return ApiResponse.ok(definitionService.archive(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/run")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportExportResponse> run(@PathVariable String id) {
        return ApiResponse.ok(exportService.run(id), MDC.get(CorrelationId.MDC_KEY));
    }
}
