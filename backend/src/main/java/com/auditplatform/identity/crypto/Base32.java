package com.auditplatform.identity.crypto;

import java.util.Arrays;
import java.util.Locale;

/**
 * RFC 4648 Base32 without padding (Authenticator-compatible).
 */
final class Base32 {

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final int[] DECODE = new int[128];

    static {
        Arrays.fill(DECODE, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            DECODE[ALPHABET[i]] = i;
        }
    }

    private Base32() {
    }

    static String encode(byte[] data) {
        if (data.length == 0) {
            return "";
        }
        StringBuilder out = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                out.append(ALPHABET[(buffer >> (bitsLeft - 5)) & 31]);
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            out.append(ALPHABET[(buffer << (5 - bitsLeft)) & 31]);
        }
        return out.toString();
    }

    static byte[] decode(String encoded) {
        String normalized = encoded.trim().toUpperCase(Locale.ROOT).replace("=", "");
        if (normalized.isEmpty()) {
            return new byte[0];
        }
        int outputLength = normalized.length() * 5 / 8;
        byte[] out = new byte[outputLength];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c >= DECODE.length || DECODE[c] < 0) {
                throw new IllegalArgumentException("Invalid Base32 character");
            }
            buffer = (buffer << 5) | DECODE[c];
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        return out;
    }
}
