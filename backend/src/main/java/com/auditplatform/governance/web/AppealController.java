package com.auditplatform.governance.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.governance.api.AppealResponse;
import com.auditplatform.governance.api.CreateAppealRequest;
import com.auditplatform.governance.api.DecideAppealRequest;
import com.auditplatform.governance.api.UpdateAppealRequest;
import com.auditplatform.governance.domain.AppealStatus;
import com.auditplatform.governance.service.AppealService;
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
@RequestMapping("/api/v1/appeals")
@Tag(name = "Appeals")
public class AppealController {

    private final AppealService appealService;

    public AppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('APPEAL_VIEW')")
    public ApiResponse<PageResponse<AppealResponse>> list(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) AppealStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(appealService.list(clientId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('APPEAL_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AppealResponse> create(@Valid @RequestBody CreateAppealRequest request) {
        return ApiResponse.ok(appealService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('APPEAL_VIEW')")
    public ApiResponse<AppealResponse> get(@PathVariable String id) {
        return ApiResponse.ok(appealService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('APPEAL_UPDATE')")
    public ApiResponse<AppealResponse> update(@PathVariable String id, @Valid @RequestBody UpdateAppealRequest request) {
        return ApiResponse.ok(appealService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAuthority('APPEAL_UPDATE')")
    public ApiResponse<AppealResponse> startReview(@PathVariable String id) {
        return ApiResponse.ok(appealService.startReview(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/decide")
    @PreAuthorize("hasAuthority('APPEAL_UPDATE')")
    public ApiResponse<AppealResponse> decide(@PathVariable String id, @Valid @RequestBody DecideAppealRequest request) {
        return ApiResponse.ok(appealService.decide(id, request), MDC.get(CorrelationId.MDC_KEY));
    }
}
