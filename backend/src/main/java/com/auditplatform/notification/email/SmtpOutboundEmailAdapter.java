package com.auditplatform.notification.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SmtpOutboundEmailAdapter implements OutboundEmailPort {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpOutboundEmailAdapter(JavaMailSender mailSender, String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body == null ? "" : body);
        mailSender.send(message);
    }
}
