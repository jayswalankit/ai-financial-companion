package com.aifinance.financialcompanion.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.aifinance.financialcompanion.exceptions.EmailDeliveryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ObjectMapper objectMapper;

    @Value("${app.email.resend-api-key:}")
    private String resendApiKey;

    @Value("${app.email.resend-api-url:https://api.resend.com/emails}")
    private String resendApiUrl;

    @Value("${app.email.timeout:10000}")
    private long timeoutMs;

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
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY is not configured");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("MAIL_FROM (or RESEND_FROM) is not configured");
        }

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "from", fromEmail.trim(),
                    "to", new String[]{toEmail},
                    "subject", subject,
                    "text", body
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create(resendApiUrl))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Resend API returned HTTP " + response.statusCode()
                        + ": " + response.body());
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize email request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Email API request was interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Could not send email through Resend HTTPS API", e);
        }

        log.info("{} email accepted by Resend for {}", logLabel, toEmail);
    }
}
