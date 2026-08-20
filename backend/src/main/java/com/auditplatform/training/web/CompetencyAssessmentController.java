package com.auditplatform.training.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.training.api.AssessmentResponse;
import com.auditplatform.training.api.CompleteAssessmentRequest;
import com.auditplatform.training.api.CreateAssessmentRequest;
import com.auditplatform.training.api.UpdateAssessmentRequest;
import com.auditplatform.training.domain.AssessmentStatus;
import com.auditplatform.training.service.CompetencyAssessmentService;
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
@RequestMapping("/api/v1")
@Tag(name = "Competency assessments")
public class CompetencyAssessmentController {

    private final CompetencyAssessmentService assessmentService;

    public CompetencyAssessmentController(CompetencyAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/competency-assessments")
    @PreAuthorize("hasAuthority('TRAINING_VIEW')")
    public ApiResponse<PageResponse<AssessmentResponse>> list(
            @RequestParam(required = false) String auditorId,
            @RequestParam(required = false) AssessmentStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(assessmentService.list(auditorId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/competency-assessments")
    @PreAuthorize("hasAuthority('TRAINING_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AssessmentResponse> create(@Valid @RequestBody CreateAssessmentRequest request) {
        return ApiResponse.ok(assessmentService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/competency-assessments/{id}")
    @PreAuthorize("hasAuthority('TRAINING_VIEW')")
    public ApiResponse<AssessmentResponse> get(@PathVariable String id) {
        return ApiResponse.ok(assessmentService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/competency-assessments/{id}")
    @PreAuthorize("hasAuthority('TRAINING_UPDATE')")
    public ApiResponse<AssessmentResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateAssessmentRequest request
    ) {
        return ApiResponse.ok(assessmentService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/competency-assessments/{id}/complete")
    @PreAuthorize("hasAuthority('TRAINING_UPDATE')")
    public ApiResponse<AssessmentResponse> complete(
            @PathVariable String id,
            @Valid @RequestBody CompleteAssessmentRequest request
    ) {
        return ApiResponse.ok(assessmentService.complete(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/auditors/{auditorId}/competency-assessments")
    @PreAuthorize("hasAuthority('TRAINING_VIEW')")
    public ApiResponse<PageResponse<AssessmentResponse>> listForAuditor(
            @PathVariable String auditorId,
            @RequestParam(required = false) AssessmentStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(assessmentService.list(auditorId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }
}
