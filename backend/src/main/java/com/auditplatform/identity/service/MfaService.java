package com.auditplatform.identity.service;

import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.api.MfaSetupResponse;
import com.auditplatform.identity.api.MfaStatusResponse;
import com.auditplatform.identity.crypto.MfaCryptoService;
import com.auditplatform.identity.crypto.TotpService;
import com.auditplatform.identity.domain.UserAccount;
import com.auditplatform.identity.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class MfaService {

    private static final String ISSUER = "Audit Platform";

    private final UserAccountRepository userAccountRepository;
    private final TotpService totpService;
    private final MfaCryptoService mfaCryptoService;
    private final PasswordEncoder passwordEncoder;

    public MfaService(
            UserAccountRepository userAccountRepository,
            TotpService totpService,
            MfaCryptoService mfaCryptoService,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.totpService = totpService;
        this.mfaCryptoService = mfaCryptoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public MfaStatusResponse status(String userId) {
        return new MfaStatusResponse(load(userId).isMfaEnabled());
    }

    @Transactional
    public MfaSetupResponse setup(String userId) {
        UserAccount user = load(userId);
        if (user.isMfaEnabled()) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "MFA is already enabled");
        }
        String secret = totpService.generateSecret();
        user.setMfaSecretEncrypted(mfaCryptoService.encrypt(secret));
        user.setMfaEnabled(false);
        userAccountRepository.save(user);
        return new MfaSetupResponse(secret, otpauthUri(user.getEmail(), secret), false);
    }

    @Transactional
    public MfaStatusResponse enable(String userId, String code) {
        UserAccount user = load(userId);
        if (user.isMfaEnabled()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "MFA is already enabled");
        }
        verifyStoredSecret(user, code);
        user.setMfaEnabled(true);
        userAccountRepository.save(user);
        return new MfaStatusResponse(true);
    }

    @Transactional
    public MfaStatusResponse disable(String userId, String code, String password) {
        UserAccount user = load(userId);
        if (!user.isMfaEnabled()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "MFA is not enabled");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid email or password");
        }
        verifyStoredSecret(user, code);
        user.setMfaEnabled(false);
        user.setMfaSecretEncrypted(null);
        userAccountRepository.save(user);
        return new MfaStatusResponse(false);
    }

    private void verifyStoredSecret(UserAccount user, String code) {
        if (user.getMfaSecretEncrypted() == null || user.getMfaSecretEncrypted().isBlank()) {
            throw new ApiException(ErrorCode.AUTH_MFA_INVALID, "Invalid MFA code");
        }
        String secret;
        try {
            secret = mfaCryptoService.decrypt(user.getMfaSecretEncrypted());
        } catch (RuntimeException ex) {
            throw new ApiException(ErrorCode.AUTH_MFA_INVALID, "Invalid MFA code");
        }
        if (!totpService.verify(secret, code == null ? "" : code.trim())) {
            throw new ApiException(ErrorCode.AUTH_MFA_INVALID, "Invalid MFA code");
        }
    }

    private UserAccount load(String userId) {
        return userAccountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "User not found"));
    }

    static String otpauthUri(String email, String secret) {
        String issuer = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8).replace("+", "%20");
        String label = URLEncoder.encode(ISSUER + ":" + email, StandardCharsets.UTF_8).replace("+", "%20");
        return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + issuer + "&digits=6&period=30";
    }
}
