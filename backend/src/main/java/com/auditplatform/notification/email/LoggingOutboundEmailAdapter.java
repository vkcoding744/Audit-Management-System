package com.auditplatform.notification.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingOutboundEmailAdapter implements OutboundEmailPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingOutboundEmailAdapter.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("Outbound email queued to={} subject={}", redact(to), subject);
    }

    private String redact(String to) {
        if (to == null || !to.contains("@")) {
            return "(redacted)";
        }
        int at = to.indexOf('@');
        return to.charAt(0) + "***" + to.substring(at);
    }
}
