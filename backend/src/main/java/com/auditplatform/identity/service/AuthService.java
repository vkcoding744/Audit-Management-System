package com.auditplatform.identity.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.api.ForgotPasswordResponse;
import com.auditplatform.identity.api.SessionResponse;
import com.auditplatform.identity.api.TokenResponse;
import com.auditplatform.identity.api.UserSummaryResponse;
import com.auditplatform.common.tenant.TenantContext;
import com.auditplatform.identity.crypto.TokenHash;
import com.auditplatform.identity.domain.AuthSession;
import com.auditplatform.identity.domain.EmailVerificationToken;
import com.auditplatform.identity.domain.PasswordResetToken;
import com.auditplatform.identity.domain.UserAccount;
import com.auditplatform.identity.domain.UserStatus;
import com.auditplatform.identity.repository.AuthSessionRepository;
import com.auditplatform.identity.repository.EmailVerificationTokenRepository;
import com.auditplatform.identity.repository.PasswordResetTokenRepository;
import com.auditplatform.identity.repository.UserAccountRepository;
import com.auditplatform.notification.email.OutboundEmailPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditPlatformProperties properties;
    private final AuditLogService auditLogService;
    private final OutboundEmailPort outboundEmailPort;
    private final String dummyPasswordHash;

    public AuthService(
            UserAccountRepository userAccountRepository,
            AuthSessionRepository authSessionRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditPlatformProperties properties,
            AuditLogService auditLogService,
            OutboundEmailPort outboundEmailPort
    ) {
        this.userAccountRepository = userAccountRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.properties = properties;
        this.auditLogService = auditLogService;
        this.outboundEmailPort = outboundEmailPort;
        this.dummyPasswordHash = passwordEncoder.encode("not-a-valid-login-attempt");
    }

    @Transactional
    public TokenResponse login(String email, String password, String mfaCode, String ip, String userAgent) {
        UserAccount user = userAccountRepository.findByEmailWithRoles(email).orElse(null);
        if (user == null) {
            passwordEncoder.matches(password, dummyPasswordHash);
            throw new ApiException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid email or password");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new ApiException(ErrorCode.AUTH_ACCOUNT_LOCKED, "Account is temporarily locked");
        }
        if (user.getStatus() == UserStatus.DISABLED || user.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.AUTH_ACCOUNT_DISABLED, "Account is disabled");
        }
        if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
            throw new ApiException(ErrorCode.AUTH_EMAIL_NOT_VERIFIED, "Account is pending activation");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            registerFailure(user);
            throw new ApiException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid email or password");
        }
        if (user.isMfaEnabled() && (mfaCode == null || mfaCode.isBlank())) {
            throw new ApiException(ErrorCode.AUTH_MFA_REQUIRED, "MFA challenge is required");
        }
        if (user.isMfaEnabled()) {
            throw new ApiException(ErrorCode.AUTH_MFA_REQUIRED, "MFA is enabled but TOTP verification is not configured");
        }
        if (properties.auth().requireEmailVerified() && user.getEmailVerifiedAt() == null) {
            throw new ApiException(ErrorCode.AUTH_EMAIL_NOT_VERIFIED, "Email is not verified");
        }
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        user.setLastLoginAt(Instant.now());
        userAccountRepository.save(user);

        IssuedTokens tokens = issueSession(user, ip, userAgent);
        TenantContext.setUserId(user.getId());
        TenantContext.setTenantId(user.getTenantId());
        auditLogService.record("LOGIN", "User", user.getId(), null, null, ip, userAgent);
        return TokenResponse.of(tokens.accessToken(), tokens.refreshToken(), jwtService.accessTokenTtlSeconds(), UserMapper.toSummary(user));
    }

    @Transactional
    public TokenResponse refresh(String refreshToken, String ip, String userAgent) {
        String hash = TokenHash.sha256(refreshToken);
        AuthSession session = authSessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new ApiException(ErrorCode.AUTH_TOKEN_INVALID, "Invalid refresh token"));
        if (session.getRevokedAt() != null) {
            authSessionRepository.revokeFamily(session.getFamilyId(), Instant.now());
            throw new ApiException(ErrorCode.AUTH_TOKEN_INVALID, "Refresh token reuse detected");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.AUTH_TOKEN_INVALID, "Refresh token expired");
        }
        UserAccount user = userAccountRepository.findByIdWithRoles(session.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.AUTH_TOKEN_INVALID, "Invalid refresh token"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            authSessionRepository.revokeAllForUser(user.getId(), Instant.now());
            throw new ApiException(ErrorCode.AUTH_ACCOUNT_DISABLED, "Account is not active");
        }
        session.setRevokedAt(Instant.now());
        authSessionRepository.save(session);

        IssuedTokens tokens = rotateSession(user, session.getFamilyId(), ip, userAgent);
        return TokenResponse.of(tokens.accessToken(), tokens.refreshToken(), jwtService.accessTokenTtlSeconds(), UserMapper.toSummary(user));
    }

    @Transactional
    public void logout(String refreshToken, String ip, String userAgent) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        authSessionRepository.findByRefreshTokenHash(TokenHash.sha256(refreshToken)).ifPresent(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(Instant.now());
                authSessionRepository.save(session);
                auditLogService.record("LOGOUT", "User", session.getUserId(), null, null, ip, userAgent);
            }
        });
    }

    @Transactional
    public void logoutAll(String userId, String ip, String userAgent) {
        authSessionRepository.revokeAllForUser(userId, Instant.now());
        auditLogService.record("LOGOUT_ALL", "User", userId, null, null, ip, userAgent);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listSessions(String userId, String currentSessionId) {
        return authSessionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(session -> new SessionResponse(
                        session.getId(),
                        session.getIpAddress(),
                        session.getUserAgent(),
                        session.getExpiresAt(),
                        session.getCreatedAt(),
                        session.getId().equals(currentSessionId),
                        session.getRevokedAt() != null
                ))
                .toList();
    }

    @Transactional
    public void revokeSession(String userId, String sessionId) {
        AuthSession session = authSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Session not found"));
        if (!session.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.SYS_FORBIDDEN, "Access denied");
        }
        if (session.getRevokedAt() == null) {
            session.setRevokedAt(Instant.now());
            authSessionRepository.save(session);
        }
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(String email) {
        String generic = "If the account exists, a reset message has been queued";
        UserAccount user = userAccountRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).orElse(null);
        if (user == null) {
            return new ForgotPasswordResponse(generic, null);
        }
        String raw = TokenHash.randomToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setTokenHash(TokenHash.sha256(raw));
        token.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        passwordResetTokenRepository.save(token);
        outboundEmailPort.send(user.getEmail(), "Password reset", "A password reset was requested for your account.");
        String exposed = properties.auth().exposeDevTokens() ? raw : null;
        return new ForgotPasswordResponse(generic, exposed);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(TokenHash.sha256(rawToken))
                .orElseThrow(() -> new ApiException(ErrorCode.AUTH_TOKEN_INVALID, "Invalid or expired token"));
        if (!token.isUsable()) {
            throw new ApiException(ErrorCode.AUTH_TOKEN_INVALID, "Invalid or expired token");
        }
        UserAccount user = userAccountRepository.findByIdWithRoles(token.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        token.setUsedAt(Instant.now());
        userAccountRepository.save(user);
        passwordResetTokenRepository.save(token);
        authSessionRepository.revokeAllForUser(user.getId(), Instant.now());
        auditLogService.record("PASSWORD_RESET", "User", user.getId(), null, null, null, null);
    }

    @Transactional
    public String issueEmailVerification(UserAccount user) {
        String raw = TokenHash.randomToken();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUserId(user.getId());
        token.setTokenHash(TokenHash.sha256(raw));
        token.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        emailVerificationTokenRepository.save(token);
        outboundEmailPort.send(user.getEmail(), "Verify your email", "Verify your Audit Platform email address.");
        return properties.auth().exposeDevTokens() ? raw : null;
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(TokenHash.sha256(rawToken))
                .orElseThrow(() -> new ApiException(ErrorCode.AUTH_TOKEN_INVALID, "Invalid or expired token"));
        if (!token.isUsable()) {
            throw new ApiException(ErrorCode.AUTH_TOKEN_INVALID, "Invalid or expired token");
        }
        UserAccount user = userAccountRepository.findById(token.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "User not found"));
        user.setEmailVerifiedAt(Instant.now());
        if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
            user.setStatus(UserStatus.ACTIVE);
        }
        token.setUsedAt(Instant.now());
        userAccountRepository.save(user);
        emailVerificationTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse me(String userId) {
        UserAccount user = userAccountRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "User not found"));
        return UserMapper.toSummary(user);
    }

    private void registerFailure(UserAccount user) {
        int failures = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(failures);
        int max = properties.auth().maxFailedLogins();
        if (failures >= max) {
            user.setLockedUntil(Instant.now().plus(properties.auth().lockoutMinutes(), ChronoUnit.MINUTES));
            user.setStatus(UserStatus.LOCKED);
        }
        userAccountRepository.save(user);
    }

    private IssuedTokens issueSession(UserAccount user, String ip, String userAgent) {
        return rotateSession(user, UUID.randomUUID().toString(), ip, userAgent);
    }

    private IssuedTokens rotateSession(UserAccount user, String familyId, String ip, String userAgent) {
        String refresh = TokenHash.randomToken();
        AuthSession session = new AuthSession();
        session.setUserId(user.getId());
        session.setTenantId(user.getTenantId());
        session.setRefreshTokenHash(TokenHash.sha256(refresh));
        session.setFamilyId(familyId);
        session.setIpAddress(ip);
        session.setUserAgent(userAgent);
        session.setExpiresAt(Instant.now().plus(properties.auth().refreshTokenDays(), ChronoUnit.DAYS));
        authSessionRepository.save(session);

        PlatformPrincipal principal = new PlatformPrincipal(
                user.getId(),
                user.getEmail(),
                user.getTenantId(),
                user.isPlatformAdmin(),
                session.getId(),
                user.permissionCodes()
        );
        String access = jwtService.createAccessToken(principal);
        return new IssuedTokens(access, refresh);
    }

    private record IssuedTokens(String accessToken, String refreshToken) {
    }
}
