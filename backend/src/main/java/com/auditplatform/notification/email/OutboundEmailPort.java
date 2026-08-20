package com.auditplatform.notification.email;

public interface OutboundEmailPort {

    void send(String to, String subject, String body);
}
