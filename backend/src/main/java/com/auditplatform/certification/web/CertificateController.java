package com.auditplatform.certification.web;

import com.auditplatform.certification.api.CertificateActionRequest;
import com.auditplatform.certification.api.CertificateResponse;
import com.auditplatform.certification.api.CreateCertificateRequest;
import com.auditplatform.certification.api.CreateSurveillanceRequest;
import com.auditplatform.certification.api.SurveillanceResponse;
import com.auditplatform.certification.domain.CertificateStatus;
import com.auditplatform.certification.service.CertificateService;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
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
@RequestMapping("/api/v1")
@Tag(name = "Certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/certificates")
    @PreAuthorize("hasAuthority('CERTIFICATE_VIEW')")
    public ApiResponse<PageResponse<CertificateResponse>> list(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) CertificateStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(certificateService.list(clientId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/certificates")
    @PreAuthorize("hasAuthority('CERTIFICATE_ISSUE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CertificateResponse> create(@Valid @RequestBody CreateCertificateRequest request) {
        return ApiResponse.ok(certificateService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/certificates/{id}")
    @PreAuthorize("hasAuthority('CERTIFICATE_VIEW')")
    public ApiResponse<CertificateResponse> get(@PathVariable String id) {
        return ApiResponse.ok(certificateService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/certificates/{id}/issue")
    @PreAuthorize("hasAuthority('CERTIFICATE_ISSUE')")
    public ApiResponse<CertificateResponse> issue(@PathVariable String id) {
        return ApiResponse.ok(certificateService.issue(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/certificates/{id}/suspend")
    @PreAuthorize("hasAuthority('CERTIFICATE_SUSPEND')")
    public ApiResponse<CertificateResponse> suspend(
            @PathVariable String id,
            @Valid @RequestBody CertificateActionRequest request
    ) {
        return ApiResponse.ok(certificateService.suspend(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/certificates/{id}/reinstate")
    @PreAuthorize("hasAuthority('CERTIFICATE_ISSUE')")
    public ApiResponse<CertificateResponse> reinstate(
            @PathVariable String id,
            @Valid @RequestBody CertificateActionRequest request
    ) {
        return ApiResponse.ok(certificateService.reinstate(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/certificates/{id}/withdraw")
    @PreAuthorize("hasAuthority('CERTIFICATE_WITHDRAW')")
    public ApiResponse<CertificateResponse> withdraw(
            @PathVariable String id,
            @Valid @RequestBody CertificateActionRequest request
    ) {
        return ApiResponse.ok(certificateService.withdraw(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/certificates/{id}/surveillance")
    @PreAuthorize("hasAuthority('CERTIFICATE_ISSUE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SurveillanceResponse> addSurveillance(
            @PathVariable String id,
            @Valid @RequestBody CreateSurveillanceRequest request
    ) {
        return ApiResponse.ok(certificateService.addSurveillance(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/surveillance/{surveillanceId}/complete")
    @PreAuthorize("hasAuthority('CERTIFICATE_ISSUE')")
    public ApiResponse<SurveillanceResponse> completeSurveillance(@PathVariable String surveillanceId) {
        return ApiResponse.ok(certificateService.completeSurveillance(surveillanceId), MDC.get(CorrelationId.MDC_KEY));
    }
}
