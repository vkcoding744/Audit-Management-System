package com.auditplatform.identity.web;

import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.security.JsonAccessDeniedHandler;
import com.auditplatform.common.security.JsonAuthenticationEntryPoint;
import com.auditplatform.common.security.JwtAuthenticationFilter;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.common.security.RateLimitFilter;
import com.auditplatform.common.security.SecurityConfig;
import com.auditplatform.common.security.TenantBindingFilter;
import com.auditplatform.common.tenant.TenantContextFilter;
import com.auditplatform.common.web.CorrelationIdFilter;
import com.auditplatform.identity.api.LoginRequest;
import com.auditplatform.identity.api.TokenResponse;
import com.auditplatform.identity.api.UserSummaryResponse;
import com.auditplatform.identity.service.AuthService;
import com.auditplatform.identity.service.JwtService;
import com.auditplatform.identity.service.MfaService;
import com.auditplatform.identity.service.UserService;
import com.auditplatform.identity.session.AuthCookieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        CorrelationIdFilter.class,
        TenantContextFilter.class,
        RateLimitFilter.class,
        JwtAuthenticationFilter.class,
        TenantBindingFilter.class,
        JwtService.class,
        AuthCookieService.class
})
@EnableConfigurationProperties(AuditPlatformProperties.class)
@TestPropertySource(properties = {
        "audit.api.docs-enabled=false",
        "audit.api.version=0.21.0",
        "audit.cors.allowed-origins=http://localhost:5173",
        "audit.rate-limit.enabled=false",
        "audit.rate-limit.requests-per-minute=120",
        "audit.auth.jwt-secret=unit-test-jwt-secret-key-32chars!!",
        "audit.auth.access-token-minutes=15",
        "audit.auth.refresh-token-days=7",
        "audit.auth.max-failed-logins=5",
        "audit.auth.lockout-minutes=15",
        "audit.auth.expose-dev-tokens=false",
        "audit.auth.require-email-verified=false",
        "audit.auth.bootstrap-admin-email=",
        "audit.auth.bootstrap-admin-password=",
        "audit.auth.cookie-sessions=true",
        "audit.auth.cookie-secure=false"
})
class AuthCookieSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private MfaService mfaService;

    @Test
    void csrfEndpointIssuesTokenCookie() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void loginWithoutCsrfIsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@example.com", "Password12345", null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("CSRF token is missing or invalid"));
    }

    @Test
    void loginWithCsrfSetsHttpOnlyCookiesAndOmitsTokens() throws Exception {
        UserSummaryResponse user = summary();
        when(authService.login(eq("admin@example.com"), eq("Password12345"), any(), any(), any()))
                .thenReturn(TokenResponse.of("access-jwt", "refresh-raw", 900, user));

        MvcResult csrf = mockMvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        String token = objectMapper.readTree(csrf.getResponse().getContentAsString()).path("data").path("token").asText();
        Cookie xsrf = csrf.getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-XSRF-TOKEN", token)
                        .cookie(xsrf)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@example.com", "Password12345", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Cookie"))
                .andExpect(jsonPath("$.data.accessToken").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(header().string("Set-Cookie", containsString("AP-ACCESS=")));
    }

    @Test
    void accessCookieAuthenticatesMe() throws Exception {
        UserSummaryResponse user = summary();
        when(authService.me("user-1")).thenReturn(user);
        String jwt = jwtService.createAccessToken(new PlatformPrincipal(
                "user-1", "admin@example.com", "t1", true, "sid", Set.of("USER_VIEW")
        ));

        mockMvc.perform(get("/api/v1/auth/me").cookie(new Cookie(AuthCookieService.ACCESS_COOKIE, jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@example.com"));
    }

    private static UserSummaryResponse summary() {
        return new UserSummaryResponse(
                "user-1", "t1", "admin@example.com", "A", "B", "ACTIVE", true, false, true,
                Set.of("PLATFORM_SUPER_ADMIN"), Set.of("USER_VIEW")
        );
    }
}
