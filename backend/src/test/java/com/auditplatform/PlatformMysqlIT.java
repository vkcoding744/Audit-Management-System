package com.auditplatform;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class PlatformMysqlIT {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.40")
            .withDatabaseName("audit_platform")
            .withUsername("audit_platform")
            .withPassword("audit_platform");

    @BeforeAll
    static void requireDocker() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is required for MySQL Testcontainers");
    }

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("audit.api.docs-enabled", () -> "false");
        registry.add("audit.api.version", () -> "0.1.0");
        registry.add("audit.cors.allowed-origins", () -> "http://localhost:5173");
        registry.add("audit.rate-limit.enabled", () -> "false");
        registry.add("audit.auth.jwt-secret", () -> "unit-test-jwt-secret-key-32chars!!");
        registry.add("audit.auth.access-token-minutes", () -> "15");
        registry.add("audit.auth.refresh-token-days", () -> "7");
        registry.add("audit.auth.max-failed-logins", () -> "5");
        registry.add("audit.auth.lockout-minutes", () -> "15");
        registry.add("audit.auth.expose-dev-tokens", () -> "false");
        registry.add("audit.auth.require-email-verified", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Test
    void flywayCreatesFoundationTables() throws Exception {
        try (Connection connection = dataSource.getConnection();
             ResultSet tenants = connection.getMetaData().getTables(null, null, "tenants", null);
             ResultSet settings = connection.getMetaData().getTables(null, null, "platform_settings", null)) {
            assertThat(tenants.next()).isTrue();
            assertThat(settings.next()).isTrue();
        }
    }

    @Test
    void healthUsesLiveDatabase() throws Exception {
        mockMvc.perform(get("/api/v1/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.database").value("UP"))
                .andExpect(jsonPath("$.data.tenantCount").value(0));
    }
}
