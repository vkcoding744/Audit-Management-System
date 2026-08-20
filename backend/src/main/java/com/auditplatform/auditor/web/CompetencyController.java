package com.auditplatform.auditor.web;

import com.auditplatform.auditor.api.CompetencyResponse;
import com.auditplatform.auditor.api.CreateCompetencyRequest;
import com.auditplatform.auditor.service.CompetencyService;
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
@Tag(name = "Auditor competencies")
public class CompetencyController {

    private final CompetencyService competencyService;

    public CompetencyController(CompetencyService competencyService) {
        this.competencyService = competencyService;
    }

    @GetMapping("/api/v1/auditors/{auditorId}/competencies")
    @PreAuthorize("hasAuthority('AUDITOR_VIEW')")
    public ApiResponse<List<CompetencyResponse>> list(@PathVariable String auditorId) {
        return ApiResponse.ok(competencyService.list(auditorId), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/auditors/{auditorId}/competencies")
    @PreAuthorize("hasAuthority('AUDITOR_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CompetencyResponse> create(
            @PathVariable String auditorId,
            @Valid @RequestBody CreateCompetencyRequest request
    ) {
        return ApiResponse.ok(competencyService.create(auditorId, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/competencies/{id}/suspend")
    @PreAuthorize("hasAuthority('AUDITOR_UPDATE')")
    public ApiResponse<CompetencyResponse> suspend(@PathVariable String id) {
        return ApiResponse.ok(competencyService.suspend(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/competencies/{id}/revoke")
    @PreAuthorize("hasAuthority('AUDITOR_UPDATE')")
    public ApiResponse<CompetencyResponse> revoke(@PathVariable String id) {
        return ApiResponse.ok(competencyService.revoke(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/api/v1/competencies/{id}")
    @PreAuthorize("hasAuthority('AUDITOR_UPDATE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        competencyService.delete(id);
    }
}
