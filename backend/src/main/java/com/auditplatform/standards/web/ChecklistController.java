package com.auditplatform.standards.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.standards.api.ChecklistItemResponse;
import com.auditplatform.standards.api.ChecklistResponse;
import com.auditplatform.standards.api.CreateChecklistItemRequest;
import com.auditplatform.standards.api.CreateChecklistRequest;
import com.auditplatform.standards.api.UpdateChecklistItemRequest;
import com.auditplatform.standards.api.UpdateChecklistRequest;
import com.auditplatform.standards.service.ChecklistService;
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
@Tag(name = "Checklists")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @GetMapping("/api/v1/schemes/{schemeId}/checklists")
    @PreAuthorize("hasAuthority('CHECKLIST_VIEW')")
    public ApiResponse<List<ChecklistResponse>> list(@PathVariable String schemeId) {
        return ApiResponse.ok(checklistService.listByScheme(schemeId), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/schemes/{schemeId}/checklists")
    @PreAuthorize("hasAuthority('CHECKLIST_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ChecklistResponse> create(
            @PathVariable String schemeId,
            @Valid @RequestBody CreateChecklistRequest request
    ) {
        return ApiResponse.ok(checklistService.create(schemeId, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/api/v1/checklists/{id}")
    @PreAuthorize("hasAuthority('CHECKLIST_VIEW')")
    public ApiResponse<ChecklistResponse> get(@PathVariable String id) {
        return ApiResponse.ok(checklistService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/api/v1/checklists/{id}")
    @PreAuthorize("hasAuthority('CHECKLIST_UPDATE')")
    public ApiResponse<ChecklistResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateChecklistRequest request
    ) {
        return ApiResponse.ok(checklistService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/checklists/{id}/activate")
    @PreAuthorize("hasAuthority('CHECKLIST_UPDATE')")
    public ApiResponse<ChecklistResponse> activate(@PathVariable String id) {
        return ApiResponse.ok(checklistService.activate(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/checklists/{id}/archive")
    @PreAuthorize("hasAuthority('CHECKLIST_UPDATE')")
    public ApiResponse<ChecklistResponse> archive(@PathVariable String id) {
        return ApiResponse.ok(checklistService.archive(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/api/v1/checklists/{id}")
    @PreAuthorize("hasAuthority('CHECKLIST_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        checklistService.delete(id);
    }

    @PostMapping("/api/v1/checklists/{id}/items")
    @PreAuthorize("hasAuthority('CHECKLIST_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ChecklistItemResponse> addItem(
            @PathVariable String id,
            @Valid @RequestBody CreateChecklistItemRequest request
    ) {
        return ApiResponse.ok(checklistService.addItem(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/api/v1/checklist-items/{id}")
    @PreAuthorize("hasAuthority('CHECKLIST_UPDATE')")
    public ApiResponse<ChecklistItemResponse> updateItem(
            @PathVariable String id,
            @Valid @RequestBody UpdateChecklistItemRequest request
    ) {
        return ApiResponse.ok(checklistService.updateItem(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/api/v1/checklist-items/{id}")
    @PreAuthorize("hasAuthority('CHECKLIST_UPDATE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable String id) {
        checklistService.deleteItem(id);
    }
}
