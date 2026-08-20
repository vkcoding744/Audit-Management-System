package com.auditplatform.identity.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.identity.api.CreateUserRequest;
import com.auditplatform.identity.api.UpdateUserRequest;
import com.auditplatform.identity.api.UserSummaryResponse;
import com.auditplatform.identity.service.UserService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ApiResponse<PageResponse<UserSummaryResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(userService.list(pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ApiResponse<UserSummaryResponse> get(@PathVariable String id) {
        return ApiResponse.ok(userService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserSummaryResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<UserSummaryResponse> update(@PathVariable String id, @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(userService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    public ApiResponse<UserSummaryResponse> deactivate(@PathVariable String id) {
        return ApiResponse.ok(userService.setActive(id, false), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    public ApiResponse<UserSummaryResponse> activate(@PathVariable String id) {
        return ApiResponse.ok(userService.setActive(id, true), MDC.get(CorrelationId.MDC_KEY));
    }
}
