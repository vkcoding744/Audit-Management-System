package com.auditplatform.identity.api;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        int expiresIn,
        UserSummaryResponse user,
        String resetToken,
        String verificationToken
) {
    public static TokenResponse of(String access, String refresh, int expiresIn, UserSummaryResponse user) {
        return new TokenResponse(access, refresh, "Bearer", expiresIn, user, null, null);
    }

    public TokenResponse withoutTokens() {
        return new TokenResponse(null, null, "Cookie", expiresIn, user, resetToken, verificationToken);
    }
}
