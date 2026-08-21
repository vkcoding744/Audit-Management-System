package com.auditplatform.ai.web;

import com.auditplatform.ai.api.AiGenerationResponse;
import com.auditplatform.ai.api.CreateAiGenerationRequest;
import com.auditplatform.ai.api.ReviewAiGenerationRequest;
import com.auditplatform.ai.api.UpdateAiGenerationRequest;
import com.auditplatform.ai.domain.AiGenerationStatus;
import com.auditplatform.ai.service.AiGenerationService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai-generations")
@Tag(name = "AI generations")
public class AiGenerationController {

    private final AiGenerationService generationService;

    public AiGenerationController(AiGenerationService generationService) {
        this.generationService = generationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AI_VIEW')")
    public ApiResponse<PageResponse<AiGenerationResponse>> list(
            @RequestParam(required = false) AiGenerationStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(generationService.list(status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('AI_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AiGenerationResponse> create(@Valid @RequestBody CreateAiGenerationRequest request) {
        return ApiResponse.ok(generationService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AI_VIEW')")
    public ApiResponse<AiGenerationResponse> get(@PathVariable String id) {
        return ApiResponse.ok(generationService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('AI_UPDATE')")
    public ApiResponse<AiGenerationResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateAiGenerationRequest request
    ) {
        return ApiResponse.ok(generationService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('AI_UPDATE')")
    public ApiResponse<AiGenerationResponse> approve(
            @PathVariable String id,
            @RequestBody(required = false) ReviewAiGenerationRequest request
    ) {
        return ApiResponse.ok(generationService.approve(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('AI_UPDATE')")
    public ApiResponse<AiGenerationResponse> reject(
            @PathVariable String id,
            @RequestBody(required = false) ReviewAiGenerationRequest request
    ) {
        return ApiResponse.ok(generationService.reject(id, request), MDC.get(CorrelationId.MDC_KEY));
    }
}
