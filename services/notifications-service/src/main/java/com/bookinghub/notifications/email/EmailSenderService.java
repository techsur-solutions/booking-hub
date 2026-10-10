package com.bookinghub.notifications.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around Spring's JavaMailSender for plain-text email delivery.
 *
 * Design intent: This class is intentionally minimal — it sets the From/To/Subject/Body
 * fields and delegates to JavaMailSender.send(). MailException is allowed to propagate
 * upward so NotificationConsumerService's retry/DLQ infrastructure treats SMTP failures
 * as retryable processing failures (per FRD F8 §Error States: "transient SMTP failure
 * → retry; persistent failure → DLQ").
 *
 * The from-address is configurable via ${notifications.from-address} (set in application.yml).
 */
@Service
public class EmailSenderService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailSenderService(
            JavaMailSender mailSender,
            @Value("${notifications.from-address}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * Sends a plain-text email.
     *
     * @param toAddress  recipient email address
     * @param subject    email subject line
     * @param body       plain-text email body
     * @throws MailException on SMTP failure — let it propagate; the consumer
     *                       treats it as a retryable processing failure
     */
    public void send(String toAddress, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message); // throws MailException on failure
    }
}
