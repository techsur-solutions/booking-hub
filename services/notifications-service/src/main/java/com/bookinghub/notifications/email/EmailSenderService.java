package com.bookinghub.notifications.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * SMTP email sending service.
 *
 * Wraps JavaMailSender to send rendered email notifications.
 * MailException propagates as retryable per the consumer's retry policy.
 */
@Slf4j
@Service
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${notifications.from-address:noreply@bookinghub.local}")
    private String fromAddress;

    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a notification email.
     *
     * @param toAddress recipient email address
     * @param subject email subject
     * @param body email body (plain text)
     * @throws MailException on SMTP failure (retryable per consumer retry policy)
     */
    public void send(String toAddress, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(body);

        log.debug("Sending email to {} with subject '{}'", toAddress, subject);
        mailSender.send(message); // throws MailException on failure — propagates as retryable
    }
}
