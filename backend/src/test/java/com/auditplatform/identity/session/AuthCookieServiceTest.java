package com.auditplatform.identity.session;

import com.auditplatform.common.config.AuditPlatformProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCookieServiceTest {

    @Test
    void writesHttpOnlySameSiteCookiesWhenEnabled() {
        AuthCookieService service = new AuthCookieService(properties(true, false));
        MockHttpServletResponse response = new MockHttpServletResponse();
        service.write(response, "access-jwt", "refresh-raw");
        assertThat(response.getHeaders("Set-Cookie"))
                .anySatisfy(header -> {
                    assertThat(header).contains("AP-ACCESS=access-jwt");
                    assertThat(header).contains("HttpOnly");
                    assertThat(header).contains("SameSite=Lax");
                    assertThat(header).doesNotContain("Secure");
                })
                .anySatisfy(header -> assertThat(header).contains("AP-REFRESH=refresh-raw"));
    }

    @Test
    void doesNotWriteWhenDisabled() {
        AuthCookieService service = new AuthCookieService(properties(false, false));
        MockHttpServletResponse response = new MockHttpServletResponse();
        service.write(response, "access-jwt", "refresh-raw");
        assertThat(response.getHeaders("Set-Cookie")).isEmpty();
    }

    private static AuditPlatformProperties properties(boolean cookies, boolean secure) {
        return new AuditPlatformProperties(
                new AuditPlatformProperties.Api(false, "0.21.0"),
                new AuditPlatformProperties.Cors("http://localhost:5173"),
                new AuditPlatformProperties.RateLimit(false, 120, "memory", "redis://localhost:6379"),
                new AuditPlatformProperties.Auth(
                        "unit-test-jwt-secret-key-32chars!!",
                        15, 7, 5, 15, false, false, "", "", "", cookies, secure
                )
        );
    }
}
