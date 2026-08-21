package com.auditplatform.identity.session;

import com.auditplatform.common.config.AuditPlatformProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieService {

    public static final String ACCESS_COOKIE = "AP-ACCESS";
    public static final String REFRESH_COOKIE = "AP-REFRESH";

    private final AuditPlatformProperties properties;

    public AuthCookieService(AuditPlatformProperties properties) {
        this.properties = properties;
    }

    public boolean cookieSessionsEnabled() {
        return properties.auth().cookieSessions();
    }

    public void write(HttpServletResponse response, String accessToken, String refreshToken) {
        if (!cookieSessionsEnabled()) {
            return;
        }
        add(response, ACCESS_COOKIE, accessToken, properties.auth().accessTokenMinutes() * 60L);
        add(response, REFRESH_COOKIE, refreshToken, properties.auth().refreshTokenDays() * 86_400L);
    }

    public void clear(HttpServletResponse response) {
        if (!cookieSessionsEnabled()) {
            return;
        }
        add(response, ACCESS_COOKIE, "", 0);
        add(response, REFRESH_COOKIE, "", 0);
    }

    public String read(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void add(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value == null ? "" : value)
                .httpOnly(true)
                .secure(properties.auth().cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
