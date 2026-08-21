package com.auditplatform.identity.crypto;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * RFC 6238 TOTP (HMAC-SHA1, 6 digits, 30-second step, ±1 window).
 */
@Service
public class TotpService {

    public static final int DIGITS = 6;
    public static final int PERIOD_SECONDS = 30;
    public static final int WINDOW = 1;
    private static final String HMAC = "HmacSHA1";

    private final SecureRandom random = new SecureRandom();

    public String generateSecret() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base32.encode(bytes);
    }

    public boolean verify(String secret, String code) {
        return verify(secret, code, Instant.now());
    }

    public boolean verify(String secret, String code, Instant now) {
        if (secret == null || code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long counter = now.getEpochSecond() / PERIOD_SECONDS;
        for (int i = -WINDOW; i <= WINDOW; i++) {
            if (constantTimeEquals(code, generateCode(secret, counter + i))) {
                return true;
            }
        }
        return false;
    }

    public String generateCode(String secret, Instant instant) {
        return generateCode(secret, instant.getEpochSecond() / PERIOD_SECONDS);
    }

    public String generateCode(String secret, long counter) {
        try {
            byte[] key = Base32.decode(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(key, HMAC));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("Unable to generate TOTP", ex);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
