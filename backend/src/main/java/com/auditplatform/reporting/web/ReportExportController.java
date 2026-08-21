package com.auditplatform.reporting.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.reporting.api.ReportExportContent;
import com.auditplatform.reporting.api.ReportExportResponse;
import com.auditplatform.reporting.domain.ReportExportStatus;
import com.auditplatform.reporting.service.ReportExportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/report-exports")
@Tag(name = "Report exports")
public class ReportExportController {

    private final ReportExportService exportService;

    public ReportExportController(ReportExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ApiResponse<PageResponse<ReportExportResponse>> list(
            @RequestParam(required = false) ReportExportStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(exportService.list(status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ApiResponse<ReportExportResponse> get(@PathVariable String id) {
        return ApiResponse.ok(exportService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ResponseEntity<InputStreamResource> download(@PathVariable String id) {
        ReportExportContent content = exportService.download(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(content.filename(), StandardCharsets.UTF_8)
                .build();
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
        if (content.sizeBytes() >= 0) {
            builder.contentLength(content.sizeBytes());
        }
        return builder.body(new InputStreamResource(content.inputStream()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('REPORT_EXPORT')")
    public ApiResponse<ReportExportResponse> cancel(@PathVariable String id) {
        return ApiResponse.ok(exportService.cancel(id), MDC.get(CorrelationId.MDC_KEY));
    }
}
