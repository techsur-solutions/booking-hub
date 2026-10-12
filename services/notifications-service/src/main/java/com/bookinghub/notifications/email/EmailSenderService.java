package com.bookinghub.notifications.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper over {@link JavaMailSender} for plain-text notification emails.
 *
 * <p><b>Deliberately does not catch {@code MailException}.</b> SMTP is this service's one
 * outbound network dependency, and a provider outage is exactly the transient failure
 * F8.2's retry policy exists to absorb. Letting the exception propagate hands control to
 * {@code RabbitConfig}'s retry interceptor, which applies the FRD-mandated 5s/30s/2min
 * backoff and, on exhaustion, routes to the DLQ via
 * {@code DatabaseTrackingMessageRecoverer} (which records {@code status='dead_lettered'}).
 * Swallowing the exception here would ack the message as delivered when no email was ever
 * sent — precisely the silent drop F8's core guarantee forbids.
 *
 * <p>Recipient addresses are never logged at INFO: an email address is personal data and
 * these logs are shipped to shared observability infrastructure. The idempotency key (an
 * opaque UUID) is the correlation handle used instead; the address itself remains
 * queryable through {@code notification_deliveries} for an operator who holds
 * {@code role_audit_viewer}.
 */
@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

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
     * @throws org.springframework.mail.MailException on any SMTP failure — intentionally
     *         propagated so the caller's retry/DLQ policy governs the outcome
     */
    public void send(String toAddress, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        log.debug("notification email dispatched, subject='{}'", subject);
    }
}
