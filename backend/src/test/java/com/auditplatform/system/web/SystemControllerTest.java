package com.auditplatform.system.web;

import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.security.JsonAccessDeniedHandler;
import com.auditplatform.common.security.JsonAuthenticationEntryPoint;
import com.auditplatform.common.security.RateLimitFilter;
import com.auditplatform.common.security.SecurityConfig;
import com.auditplatform.common.tenant.TenantContextFilter;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.common.web.CorrelationIdFilter;
import com.auditplatform.system.api.SystemHealthResponse;
import com.auditplatform.system.service.SystemHealthService;
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

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SystemController.class,
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
        RateLimitFilter.class
})
@EnableConfigurationProperties(AuditPlatformProperties.class)
@TestPropertySource(properties = {
        "audit.api.docs-enabled=false",
        "audit.api.version=0.1.0",
        "audit.cors.allowed-origins=http://localhost:5173",
        "audit.rate-limit.enabled=false",
        "audit.rate-limit.requests-per-minute=120"
})
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemHealthService healthService;

    @Test
    void healthReturnsEnvelopeWhenDatabaseIsUp() throws Exception {
        when(healthService.health()).thenReturn(new SystemHealthResponse("UP", "UP", 0));

        mockMvc.perform(get("/api/v1/system/health").header(CorrelationId.HEADER, "cid-health"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationId.HEADER, "cid-health"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.database").value("UP"))
                .andExpect(jsonPath("$.meta.correlationId").value("cid-health"))
                .andExpect(jsonPath("$.meta.timestamp", notNullValue()));
    }

    @Test
    void healthReturns503WhenDatabaseIsDown() throws Exception {
        when(healthService.health()).thenReturn(new SystemHealthResponse("DOWN", "DOWN", -1));

        mockMvc.perform(get("/api/v1/system/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DOWN"));
    }

    @Test
    void infoIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/system/info").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.application").value("audit-platform"))
                .andExpect(jsonPath("$.data.apiVersion").value("0.1.0"));
    }

    @Test
    void protectedApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("SYS_UNAUTHORIZED"));
    }
}
