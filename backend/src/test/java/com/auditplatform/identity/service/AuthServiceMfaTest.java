package com.auditplatform.identity.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.crypto.MfaCryptoService;
import com.auditplatform.identity.crypto.TotpService;
import com.auditplatform.identity.domain.UserAccount;
import com.auditplatform.identity.domain.UserStatus;
import com.auditplatform.identity.repository.AuthSessionRepository;
import com.auditplatform.identity.repository.EmailVerificationTokenRepository;
import com.auditplatform.identity.repository.PasswordResetTokenRepository;
import com.auditplatform.identity.repository.UserAccountRepository;
import com.auditplatform.notification.email.OutboundEmailPort;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceMfaTest {

    private static final String SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    @Test
    void loginRequiresMfaCodeWhenEnabled() {
        UserAccount user = mfaUser();
        AuthService service = service(user, totpThatRejects());

        assertThatThrownBy(() -> service.login("user@example.com", "Password12345", null, "127.0.0.1", "test"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_MFA_REQUIRED);
    }

    @Test
    void loginRejectsInvalidMfaCode() {
        UserAccount user = mfaUser();
        AuthService service = service(user, totpThatRejects());

        assertThatThrownBy(() -> service.login("user@example.com", "Password12345", "000000", "127.0.0.1", "test"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_MFA_INVALID);
    }

    @Test
    void loginAcceptsValidMfaCode() {
        UserAccount user = mfaUser();
        TotpService totpService = mock(TotpService.class);
        when(totpService.verify(SECRET, "287082")).thenReturn(true);
        AuthService service = service(user, totpService);
        service.login("user@example.com", "Password12345", "287082", "127.0.0.1", "test");
        verify(totpService).verify(SECRET, "287082");
    }

    private static UserAccount mfaUser() {
        UserAccount user = new UserAccount();
        ReflectionTestUtils.setField(user, "id", "user-1");
        user.setEmail("user@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("Ada");
        user.setLastName("Lovelace");
        user.setStatus(UserStatus.ACTIVE);
        user.setTenantId("tenant-1");
        user.setMfaEnabled(true);
        user.setMfaSecretEncrypted("enc");
        return user;
    }

    private TotpService totpThatRejects() {
        TotpService totpService = mock(TotpService.class);
        when(totpService.verify(any(), any())).thenReturn(false);
        return totpService;
    }

    private AuthService service(UserAccount user, TotpService totpService) {
        UserAccountRepository users = mock(UserAccountRepository.class);
        when(users.findByEmailWithRoles("user@example.com")).thenReturn(Optional.of(user));
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("dummy");
        when(encoder.matches("Password12345", "hash")).thenReturn(true);

        MfaCryptoService crypto = mock(MfaCryptoService.class);
        when(crypto.decrypt("enc")).thenReturn(SECRET);

        AuthSessionRepository sessions = mock(AuthSessionRepository.class);
        when(sessions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JwtService jwtService = mock(JwtService.class);
        when(jwtService.createAccessToken(any())).thenReturn("access");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900);

        AuditPlatformProperties properties = new AuditPlatformProperties(
                new AuditPlatformProperties.Api(false, "0.20.0"),
                new AuditPlatformProperties.Cors("http://localhost:5173"),
                new AuditPlatformProperties.RateLimit(false, 120, "memory", "redis://localhost:6379"),
                new AuditPlatformProperties.Auth(
                        "unit-test-jwt-secret-key-32chars!!",
                        15, 7, 5, 15, false, false, "", "", "mfa-key", false, false
                )
        );

        return new AuthService(
                users,
                sessions,
                mock(PasswordResetTokenRepository.class),
                mock(EmailVerificationTokenRepository.class),
                encoder,
                jwtService,
                properties,
                mock(AuditLogService.class),
                mock(OutboundEmailPort.class),
                totpService,
                crypto
        );
    }
}
