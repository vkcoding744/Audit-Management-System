package com.auditplatform.identity.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.common.web.ClientRequest;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.identity.api.ForgotPasswordRequest;
import com.auditplatform.identity.api.ForgotPasswordResponse;
import com.auditplatform.identity.api.LoginRequest;
import com.auditplatform.identity.api.LogoutRequest;
import com.auditplatform.identity.api.MfaDisableRequest;
import com.auditplatform.identity.api.MfaEnableRequest;
import com.auditplatform.identity.api.MfaSetupResponse;
import com.auditplatform.identity.api.MfaStatusResponse;
import com.auditplatform.identity.api.RefreshRequest;
import com.auditplatform.identity.api.ResetPasswordRequest;
import com.auditplatform.identity.api.SessionResponse;
import com.auditplatform.identity.api.TokenResponse;
import com.auditplatform.identity.api.UserSummaryResponse;
import com.auditplatform.identity.api.VerifyEmailIssueResponse;
import com.auditplatform.identity.api.VerifyEmailRequest;
import com.auditplatform.identity.service.AuthService;
import com.auditplatform.identity.service.MfaService;
import com.auditplatform.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final MfaService mfaService;

    public AuthController(AuthService authService, UserService userService, MfaService mfaService) {
        this.authService = authService;
        this.userService = userService;
        this.mfaService = mfaService;
    }

    @PostMapping("/login")
    @Operation(summary = "Sign in with email and password")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        TokenResponse data = authService.login(
                request.email(),
                request.password(),
                request.mfaCode(),
                ClientRequest.ipAddress(http),
                ClientRequest.userAgent(http)
        );
        return ApiResponse.ok(data, MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest http) {
        TokenResponse data = authService.refresh(request.refreshToken(), ClientRequest.ipAddress(http), ClientRequest.userAgent(http));
        return ApiResponse.ok(data, MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody(required = false) LogoutRequest request, HttpServletRequest http) {
        String token = request == null ? null : request.refreshToken();
        authService.logout(token, ClientRequest.ipAddress(http), ClientRequest.userAgent(http));
        return ApiResponse.ok(null, MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@AuthenticationPrincipal PlatformPrincipal principal, HttpServletRequest http) {
        authService.logoutAll(principal.userId(), ClientRequest.ipAddress(http), ClientRequest.userAgent(http));
        return ApiResponse.ok(null, MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.ok(authService.forgotPassword(request.email()), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ApiResponse.ok(null, MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ApiResponse.ok(null, MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/resend-verification")
    public ApiResponse<VerifyEmailIssueResponse> resend(@AuthenticationPrincipal PlatformPrincipal principal) {
        return ApiResponse.ok(userService.resendVerification(principal.userId()), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/me")
    public ApiResponse<UserSummaryResponse> me(@AuthenticationPrincipal PlatformPrincipal principal) {
        return ApiResponse.ok(authService.me(principal.userId()), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/mfa")
    public ApiResponse<MfaStatusResponse> mfaStatus(@AuthenticationPrincipal PlatformPrincipal principal) {
        return ApiResponse.ok(mfaService.status(principal.userId()), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/mfa/setup")
    public ApiResponse<MfaSetupResponse> mfaSetup(@AuthenticationPrincipal PlatformPrincipal principal) {
        return ApiResponse.ok(mfaService.setup(principal.userId()), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/mfa/enable")
    public ApiResponse<MfaStatusResponse> mfaEnable(
            @AuthenticationPrincipal PlatformPrincipal principal,
            @Valid @RequestBody MfaEnableRequest request
    ) {
        return ApiResponse.ok(mfaService.enable(principal.userId(), request.code()), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/mfa/disable")
    public ApiResponse<MfaStatusResponse> mfaDisable(
            @AuthenticationPrincipal PlatformPrincipal principal,
            @Valid @RequestBody MfaDisableRequest request
    ) {
        return ApiResponse.ok(
                mfaService.disable(principal.userId(), request.code(), request.password()),
                MDC.get(CorrelationId.MDC_KEY)
        );
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionResponse>> sessions(@AuthenticationPrincipal PlatformPrincipal principal) {
        return ApiResponse.ok(authService.listSessions(principal.userId(), principal.sessionId()), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> revokeSession(
            @AuthenticationPrincipal PlatformPrincipal principal,
            @PathVariable String id
    ) {
        authService.revokeSession(principal.userId(), id);
        return ApiResponse.ok(null, MDC.get(CorrelationId.MDC_KEY));
    }
}
