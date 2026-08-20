package com.auditplatform.auditor.web;

import com.auditplatform.auditor.api.AuditorResponse;
import com.auditplatform.auditor.api.CreateAuditorRequest;
import com.auditplatform.auditor.api.CreateQualificationRequest;
import com.auditplatform.auditor.api.EligibilityResponse;
import com.auditplatform.auditor.api.QualificationResponse;
import com.auditplatform.auditor.api.UpdateAuditorRequest;
import com.auditplatform.auditor.domain.AuditorStatus;
import com.auditplatform.auditor.service.AuditorEligibilityService;
import com.auditplatform.auditor.service.AuditorService;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auditors")
@Tag(name = "Auditors")
public class AuditorController {

    private final AuditorService auditorService;
    private final AuditorEligibilityService eligibilityService;

    public AuditorController(AuditorService auditorService, AuditorEligibilityService eligibilityService) {
        this.auditorService = auditorService;
        this.eligibilityService = eligibilityService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDITOR_VIEW')")
    public ApiResponse<PageResponse<AuditorResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) AuditorStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(auditorService.list(q, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDITOR_VIEW')")
    public ApiResponse<AuditorResponse> get(@PathVariable String id) {
        return ApiResponse.ok(auditorService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}/eligibility")
    @PreAuthorize("hasAuthority('AUDITOR_VIEW')")
    public ApiResponse<EligibilityResponse> eligibility(
            @PathVariable String id,
            @RequestParam(required = false) String standardId,
            @RequestParam(required = false) String schemeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate on
    ) {
        return ApiResponse.ok(eligibilityService.evaluate(id, standardId, schemeId, on), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('AUDITOR_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuditorResponse> create(@Valid @RequestBody CreateAuditorRequest request) {
        return ApiResponse.ok(auditorService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDITOR_UPDATE')")
    public ApiResponse<AuditorResponse> update(@PathVariable String id, @Valid @RequestBody UpdateAuditorRequest request) {
        return ApiResponse.ok(auditorService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDITOR_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        auditorService.delete(id);
    }

    @GetMapping("/{id}/qualifications")
    @PreAuthorize("hasAuthority('AUDITOR_VIEW')")
    public ApiResponse<List<QualificationResponse>> qualifications(@PathVariable String id) {
        return ApiResponse.ok(auditorService.listQualifications(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/qualifications")
    @PreAuthorize("hasAuthority('AUDITOR_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QualificationResponse> addQualification(
            @PathVariable String id,
            @Valid @RequestBody CreateQualificationRequest request
    ) {
        return ApiResponse.ok(auditorService.addQualification(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/qualifications/{qualificationId}")
    @PreAuthorize("hasAuthority('AUDITOR_UPDATE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQualification(@PathVariable String qualificationId) {
        auditorService.deleteQualification(qualificationId);
    }
}
