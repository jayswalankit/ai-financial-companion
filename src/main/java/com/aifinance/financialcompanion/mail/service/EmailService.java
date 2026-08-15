package com.aifinance.financialcompanion.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.aifinance.financialcompanion.exceptions.EmailDeliveryException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendSimpleEmail(
            String fromEmail,
            String toEmail,
            String subject,
            String body,
            String logLabel
    ) {
        try {
            sendSimpleEmailInternal(fromEmail, toEmail, subject, body, logLabel);
        } catch (Exception e) {
            throw new EmailDeliveryException(
                    "Failed to send " + logLabel + " email to " + toEmail,
                    e
            );
        }
    }

    @Async
    public CompletableFuture<Void> sendSimpleEmailAsync(
            String fromEmail,
            String toEmail,
            String subject,
            String body,
            String logLabel
    ) {
        try {
            sendSimpleEmailInternal(fromEmail, toEmail, subject, body, logLabel);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send {} email to {}", logLabel, toEmail, e);
            return CompletableFuture.failedFuture(
                    new EmailDeliveryException(
                            "Failed to send " + logLabel + " email to " + toEmail,
                            e
                    )
            );
        }
    }

    private void sendSimpleEmailInternal(
            String fromEmail,
            String toEmail,
            String subject,
            String body,
            String logLabel
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            message.setFrom(fromEmail.trim());
        }
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        log.info("{} email sent to {}", logLabel, toEmail);
    }
}
