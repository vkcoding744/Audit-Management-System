package com.auditplatform.governance.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.governance.api.CreateImpartialityRequest;
import com.auditplatform.governance.api.ImpartialityResponse;
import com.auditplatform.governance.api.NotesRequest;
import com.auditplatform.governance.api.UpdateImpartialityRequest;
import com.auditplatform.governance.domain.ImpartialityStatus;
import com.auditplatform.governance.service.ImpartialityService;
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
@RequestMapping("/api/v1/impartiality-records")
@Tag(name = "Impartiality")
public class ImpartialityController {

    private final ImpartialityService impartialityService;

    public ImpartialityController(ImpartialityService impartialityService) {
        this.impartialityService = impartialityService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ApiResponse<PageResponse<ImpartialityResponse>> list(
            @RequestParam(required = false) ImpartialityStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(impartialityService.list(status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RISK_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ImpartialityResponse> create(@Valid @RequestBody CreateImpartialityRequest request) {
        return ApiResponse.ok(impartialityService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RISK_VIEW')")
    public ApiResponse<ImpartialityResponse> get(@PathVariable String id) {
        return ApiResponse.ok(impartialityService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('RISK_UPDATE')")
    public ApiResponse<ImpartialityResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateImpartialityRequest request
    ) {
        return ApiResponse.ok(impartialityService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAuthority('RISK_UPDATE')")
    public ApiResponse<ImpartialityResponse> startReview(
            @PathVariable String id,
            @RequestBody(required = false) NotesRequest request
    ) {
        return ApiResponse.ok(impartialityService.startReview(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('RISK_UPDATE')")
    public ApiResponse<ImpartialityResponse> close(
            @PathVariable String id,
            @RequestBody(required = false) NotesRequest request
    ) {
        return ApiResponse.ok(impartialityService.close(id, request), MDC.get(CorrelationId.MDC_KEY));
    }
}
