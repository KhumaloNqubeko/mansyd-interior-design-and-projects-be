package com.carpenter.business.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class EmailSenderConfiguration {
    @Bean
    EmailSender emailSender(@Value("${BREVO_API_KEY:}") String apiKey,
                            @Value("${BREVO_SENDER_EMAIL:}") String senderEmail,
                            @Value("${BREVO_SENDER_NAME:Carpentry Business}") String senderName,
                            ObjectMapper objectMapper) {
        boolean anyConfigured = !apiKey.isBlank() || !senderEmail.isBlank();
        boolean allConfigured = !apiKey.isBlank() && !senderEmail.isBlank();
        if (anyConfigured && !allConfigured) {
            throw new IllegalStateException("BREVO_API_KEY and BREVO_SENDER_EMAIL must both be configured.");
        }
        return allConfigured
                ? new BrevoEmailSender(apiKey, senderEmail, senderName, objectMapper)
                : new LoggingEmailSender();
    }
}
