package com.auditplatform.auditor.web;

import com.auditplatform.auditor.api.AvailabilityResponse;
import com.auditplatform.auditor.api.CreateAvailabilityRequest;
import com.auditplatform.auditor.service.AvailabilityService;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Auditor availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/api/v1/auditors/{auditorId}/availability")
    @PreAuthorize("hasAuthority('AUDITOR_VIEW')")
    public ApiResponse<List<AvailabilityResponse>> list(@PathVariable String auditorId) {
        return ApiResponse.ok(availabilityService.list(auditorId), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/auditors/{auditorId}/availability")
    @PreAuthorize("hasAuthority('AUDITOR_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AvailabilityResponse> create(
            @PathVariable String auditorId,
            @Valid @RequestBody CreateAvailabilityRequest request
    ) {
        return ApiResponse.ok(availabilityService.create(auditorId, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/api/v1/availability/{id}")
    @PreAuthorize("hasAuthority('AUDITOR_UPDATE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        availabilityService.delete(id);
    }
}
