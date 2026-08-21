package com.auditplatform.search.web;

import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.security.JsonAccessDeniedHandler;
import com.auditplatform.common.security.JsonAuthenticationEntryPoint;
import com.auditplatform.common.security.JwtAuthenticationFilter;
import com.auditplatform.common.security.RateLimitFilter;
import com.auditplatform.common.security.SecurityConfig;
import com.auditplatform.common.security.TenantBindingFilter;
import com.auditplatform.common.tenant.TenantContextFilter;
import com.auditplatform.common.web.CorrelationIdFilter;
import com.auditplatform.identity.service.JwtService;
import com.auditplatform.search.api.SearchResponse;
import com.auditplatform.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SearchController.class,
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
        JwtService.class
})
@EnableConfigurationProperties(AuditPlatformProperties.class)
@TestPropertySource(properties = {
        "audit.api.docs-enabled=false",
        "audit.api.version=0.22.0",
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
        "audit.auth.bootstrap-admin-password="
})
class SearchControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Test
    void searchUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/search").param("q", "acme")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CLIENT_VIEW")
    void searchForbiddenWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/search").param("q", "acme"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("SYS_FORBIDDEN"));
    }

    @Test
    @WithMockUser(authorities = "SEARCH_VIEW")
    void searchAllowedWithPermission() throws Exception {
        when(searchService.search("acme", null)).thenReturn(new SearchResponse("mysql", "acme", List.of()));
        mockMvc.perform(get("/api/v1/search").param("q", "acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.provider").value("mysql"));
    }
}
