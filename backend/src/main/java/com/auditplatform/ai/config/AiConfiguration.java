package com.auditplatform.ai.config;

import com.auditplatform.ai.spi.AiGenerationPort;
import com.auditplatform.ai.spi.StubAiGenerationAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfiguration {

    @Bean
    @ConditionalOnProperty(name = "audit.ai.provider", havingValue = "stub", matchIfMissing = true)
    public AiGenerationPort stubAiGenerationAdapter(AiProperties properties) {
        return new StubAiGenerationAdapter(properties);
    }
}
