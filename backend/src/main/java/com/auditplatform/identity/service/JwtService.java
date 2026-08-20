package com.auditplatform.identity.service;

import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.security.PlatformPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class JwtService {

    private final AuditPlatformProperties properties;
    private final SecretKey key;

    public JwtService(AuditPlatformProperties properties) {
        this.properties = properties;
        String secret = properties.auth().jwtSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("AUDIT_PLATFORM_JWT_SECRET must be at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(PlatformPrincipal principal) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(properties.auth().accessTokenMinutes() * 60L);
        return Jwts.builder()
                .subject(principal.userId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expires))
                .claim("email", principal.email())
                .claim("tid", principal.tenantId())
                .claim("sid", principal.sessionId())
                .claim("plat", principal.platformAdmin())
                .claim("perms", List.copyOf(principal.permissions()))
                .signWith(key)
                .compact();
    }

    public PlatformPrincipal parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            List<String> perms = claims.get("perms", List.class);
            Set<String> permissions = perms == null ? Set.of() : Set.copyOf(perms);
            Boolean platform = claims.get("plat", Boolean.class);
            return new PlatformPrincipal(
                    claims.getSubject(),
                    claims.get("email", String.class),
                    claims.get("tid", String.class),
                    Boolean.TRUE.equals(platform),
                    claims.get("sid", String.class),
                    permissions
            );
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public int accessTokenTtlSeconds() {
        return properties.auth().accessTokenMinutes() * 60;
    }
}
