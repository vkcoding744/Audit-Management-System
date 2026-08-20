package com.auditplatform.training.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.training.api.CompleteTrainingRequest;
import com.auditplatform.training.api.CreateTrainingRecordRequest;
import com.auditplatform.training.api.TrainingRecordResponse;
import com.auditplatform.training.api.UpdateTrainingRecordRequest;
import com.auditplatform.training.domain.TrainingStatus;
import com.auditplatform.training.service.TrainingRecordService;
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
@Tag(name = "Training records")
public class TrainingRecordController {

    private final TrainingRecordService trainingRecordService;

    public TrainingRecordController(TrainingRecordService trainingRecordService) {
        this.trainingRecordService = trainingRecordService;
    }

    @GetMapping("/training-records")
    @PreAuthorize("hasAuthority('TRAINING_VIEW')")
    public ApiResponse<PageResponse<TrainingRecordResponse>> list(
            @RequestParam(required = false) String auditorId,
            @RequestParam(required = false) TrainingStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(trainingRecordService.list(auditorId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/training-records")
    @PreAuthorize("hasAuthority('TRAINING_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TrainingRecordResponse> create(@Valid @RequestBody CreateTrainingRecordRequest request) {
        return ApiResponse.ok(trainingRecordService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/training-records/{id}")
    @PreAuthorize("hasAuthority('TRAINING_VIEW')")
    public ApiResponse<TrainingRecordResponse> get(@PathVariable String id) {
        return ApiResponse.ok(trainingRecordService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/training-records/{id}")
    @PreAuthorize("hasAuthority('TRAINING_UPDATE')")
    public ApiResponse<TrainingRecordResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateTrainingRecordRequest request
    ) {
        return ApiResponse.ok(trainingRecordService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/training-records/{id}/complete")
    @PreAuthorize("hasAuthority('TRAINING_UPDATE')")
    public ApiResponse<TrainingRecordResponse> complete(
            @PathVariable String id,
            @RequestBody(required = false) CompleteTrainingRequest request
    ) {
        return ApiResponse.ok(trainingRecordService.complete(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/training-records/{id}/cancel")
    @PreAuthorize("hasAuthority('TRAINING_UPDATE')")
    public ApiResponse<TrainingRecordResponse> cancel(@PathVariable String id) {
        return ApiResponse.ok(trainingRecordService.cancel(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/auditors/{auditorId}/training-records")
    @PreAuthorize("hasAuthority('TRAINING_VIEW')")
    public ApiResponse<PageResponse<TrainingRecordResponse>> listForAuditor(
            @PathVariable String auditorId,
            @RequestParam(required = false) TrainingStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(trainingRecordService.list(auditorId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }
}
