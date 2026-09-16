package com.carpenter.business.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BrevoEmailSender implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(BrevoEmailSender.class);
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final ObjectMapper objectMapper;

    BrevoEmailSender(String apiKey, String senderEmail, String senderName, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.objectMapper = objectMapper;
    }

    @Override
    public void send(String recipient, String subject, String htmlContent) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(payload(recipient, subject, htmlContent)))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Brevo rejected the verification email with status {}: {}",
                        response.statusCode(), response.body());
                throw new IllegalStateException("The verification email could not be sent.");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("The verification email could not be sent.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("The verification email could not be sent.", ex);
        }
    }

    private String payload(String recipient, String subject, String htmlContent) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", recipient)),
                    "subject", subject,
                    "htmlContent", htmlContent));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("The verification email could not be prepared.", ex);
        }
    }
}
