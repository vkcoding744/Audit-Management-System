package com.auditplatform.governance.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.governance.api.CloseComplaintRequest;
import com.auditplatform.governance.api.ComplaintResponse;
import com.auditplatform.governance.api.CreateComplaintRequest;
import com.auditplatform.governance.api.UpdateComplaintRequest;
import com.auditplatform.governance.domain.ComplaintStatus;
import com.auditplatform.governance.service.ComplaintService;
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
@RequestMapping("/api/v1/complaints")
@Tag(name = "Complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPLAINT_VIEW')")
    public ApiResponse<PageResponse<ComplaintResponse>> list(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) ComplaintStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(complaintService.list(clientId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COMPLAINT_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ComplaintResponse> create(@Valid @RequestBody CreateComplaintRequest request) {
        return ApiResponse.ok(complaintService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPLAINT_VIEW')")
    public ApiResponse<ComplaintResponse> get(@PathVariable String id) {
        return ApiResponse.ok(complaintService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPLAINT_UPDATE')")
    public ApiResponse<ComplaintResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateComplaintRequest request
    ) {
        return ApiResponse.ok(complaintService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAuthority('COMPLAINT_UPDATE')")
    public ApiResponse<ComplaintResponse> startReview(@PathVariable String id) {
        return ApiResponse.ok(complaintService.startReview(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('COMPLAINT_UPDATE')")
    public ApiResponse<ComplaintResponse> close(
            @PathVariable String id,
            @Valid @RequestBody CloseComplaintRequest request
    ) {
        return ApiResponse.ok(complaintService.close(id, request), MDC.get(CorrelationId.MDC_KEY));
    }
}
