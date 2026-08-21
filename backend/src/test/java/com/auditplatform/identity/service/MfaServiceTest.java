package com.auditplatform.identity.service;

import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.api.MfaSetupResponse;
import com.auditplatform.identity.crypto.MfaCryptoService;
import com.auditplatform.identity.crypto.TotpService;
import com.auditplatform.identity.domain.UserAccount;
import com.auditplatform.identity.domain.UserStatus;
import com.auditplatform.identity.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MfaServiceTest {

    @Test
    void setupStoresEncryptedSecretWithoutEnabling() {
        UserAccount user = user(false, null);
        TotpService totp = mock(TotpService.class);
        when(totp.generateSecret()).thenReturn("SECRETBASE32VALUE");
        MfaCryptoService crypto = mock(MfaCryptoService.class);
        when(crypto.encrypt("SECRETBASE32VALUE")).thenReturn("enc");
        UserAccountRepository users = repo(user);

        MfaSetupResponse response = new MfaService(users, totp, crypto, mock(PasswordEncoder.class)).setup("user-1");

        assertThat(response.mfaEnabled()).isFalse();
        assertThat(response.secret()).isEqualTo("SECRETBASE32VALUE");
        assertThat(response.otpauthUri()).contains("otpauth://totp/");
        assertThat(user.getMfaSecretEncrypted()).isEqualTo("enc");
        assertThat(user.isMfaEnabled()).isFalse();
    }

    @Test
    void enableRequiresValidCode() {
        UserAccount user = user(false, "enc");
        TotpService totp = mock(TotpService.class);
        when(totp.verify("plain", "123456")).thenReturn(false);
        MfaCryptoService crypto = mock(MfaCryptoService.class);
        when(crypto.decrypt("enc")).thenReturn("plain");

        assertThatThrownBy(() -> new MfaService(repo(user), totp, crypto, mock(PasswordEncoder.class)).enable("user-1", "123456"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_MFA_INVALID);
    }

    @Test
    void disableClearsSecretWhenPasswordAndCodeMatch() {
        UserAccount user = user(true, "enc");
        TotpService totp = mock(TotpService.class);
        when(totp.verify("plain", "123456")).thenReturn(true);
        MfaCryptoService crypto = mock(MfaCryptoService.class);
        when(crypto.decrypt("enc")).thenReturn("plain");
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.matches("Password12345", "hash")).thenReturn(true);

        new MfaService(repo(user), totp, crypto, encoder).disable("user-1", "123456", "Password12345");

        assertThat(user.isMfaEnabled()).isFalse();
        assertThat(user.getMfaSecretEncrypted()).isNull();
    }

    private static UserAccount user(boolean enabled, String secret) {
        UserAccount user = new UserAccount();
        ReflectionTestUtils.setField(user, "id", "user-1");
        user.setEmail("user@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("Ada");
        user.setLastName("Lovelace");
        user.setStatus(UserStatus.ACTIVE);
        user.setMfaEnabled(enabled);
        user.setMfaSecretEncrypted(secret);
        return user;
    }

    private static UserAccountRepository repo(UserAccount user) {
        UserAccountRepository users = mock(UserAccountRepository.class);
        when(users.findByIdAndDeletedAtIsNull("user-1")).thenReturn(Optional.of(user));
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return users;
    }
}
