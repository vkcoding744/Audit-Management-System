package com.auditplatform.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "audit.api.docs-enabled", havingValue = "true")
public class OpenApiConfig {

    @Bean
    public OpenAPI auditPlatformOpenApi(AuditPlatformProperties properties) {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        return new OpenAPI()
                .info(new Info()
                        .title("Audit Platform API")
                        .version(properties.api().version())
                        .description("Multi-tenant audit and certification management API"))
                .components(new Components().addSecuritySchemes("bearer-jwt", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
