package com.auditplatform.standards.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.standards.api.ClauseResponse;
import com.auditplatform.standards.api.CreateClauseRequest;
import com.auditplatform.standards.api.UpdateClauseRequest;
import com.auditplatform.standards.service.ClauseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Clauses")
public class ClauseController {

    private final ClauseService clauseService;

    public ClauseController(ClauseService clauseService) {
        this.clauseService = clauseService;
    }

    @GetMapping("/api/v1/standards/{standardId}/clauses")
    @PreAuthorize("hasAuthority('STANDARD_VIEW')")
    public ApiResponse<List<ClauseResponse>> list(@PathVariable String standardId) {
        return ApiResponse.ok(clauseService.listByStandard(standardId), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/standards/{standardId}/clauses")
    @PreAuthorize("hasAuthority('STANDARD_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClauseResponse> create(
            @PathVariable String standardId,
            @Valid @RequestBody CreateClauseRequest request
    ) {
        return ApiResponse.ok(clauseService.create(standardId, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/api/v1/clauses/{id}")
    @PreAuthorize("hasAuthority('STANDARD_UPDATE')")
    public ApiResponse<ClauseResponse> update(@PathVariable String id, @Valid @RequestBody UpdateClauseRequest request) {
        return ApiResponse.ok(clauseService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/api/v1/clauses/{id}")
    @PreAuthorize("hasAuthority('STANDARD_UPDATE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        clauseService.delete(id);
    }
}
