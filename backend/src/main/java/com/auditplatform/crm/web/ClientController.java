package com.auditplatform.crm.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.crm.api.ClientDashboardResponse;
import com.auditplatform.crm.api.ClientResponse;
import com.auditplatform.crm.api.CreateClientRequest;
import com.auditplatform.crm.api.UpdateClientRequest;
import com.auditplatform.crm.domain.ClientStatus;
import com.auditplatform.crm.service.ClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENT_VIEW')")
    public ApiResponse<PageResponse<ClientResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ClientStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(clientService.list(q, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_VIEW')")
    public ApiResponse<ClientResponse> get(@PathVariable String id) {
        return ApiResponse.ok(clientService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasAuthority('CLIENT_VIEW')")
    public ApiResponse<ClientDashboardResponse> dashboard(@PathVariable String id) {
        return ApiResponse.ok(clientService.dashboard(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENT_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        return ApiResponse.ok(clientService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_UPDATE')")
    public ApiResponse<ClientResponse> update(@PathVariable String id, @Valid @RequestBody UpdateClientRequest request) {
        return ApiResponse.ok(clientService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('CLIENT_UPDATE')")
    public ApiResponse<ClientResponse> activate(@PathVariable String id) {
        return ApiResponse.ok(clientService.setStatus(id, ClientStatus.ACTIVE), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('CLIENT_UPDATE')")
    public ApiResponse<ClientResponse> suspend(@PathVariable String id) {
        return ApiResponse.ok(clientService.setStatus(id, ClientStatus.SUSPENDED), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENT_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        clientService.delete(id);
    }
}
