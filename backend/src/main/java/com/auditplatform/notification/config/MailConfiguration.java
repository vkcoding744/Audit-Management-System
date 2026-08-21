package com.auditplatform.notification.config;

import com.auditplatform.notification.email.LoggingOutboundEmailAdapter;
import com.auditplatform.notification.email.OutboundEmailPort;
import com.auditplatform.notification.email.SmtpOutboundEmailAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfiguration {

    @Bean
    @ConditionalOnProperty(name = "audit.mail.provider", havingValue = "logging", matchIfMissing = true)
    public OutboundEmailPort loggingOutboundEmailAdapter() {
        return new LoggingOutboundEmailAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "audit.mail.provider", havingValue = "smtp")
    @ConditionalOnBean(JavaMailSender.class)
    public OutboundEmailPort smtpOutboundEmailAdapter(JavaMailSender mailSender, MailProperties properties) {
        return new SmtpOutboundEmailAdapter(mailSender, properties.fromOrDefault());
    }
}
