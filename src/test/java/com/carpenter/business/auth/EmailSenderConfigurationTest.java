package com.carpenter.business.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class EmailSenderConfigurationTest {
    private final EmailSenderConfiguration configuration = new EmailSenderConfiguration();

    @Test
    void usesBrevoWheneverRequiredSettingsAreConfigured() {
        assertThat(configuration.emailSender("key", "verified@example.com", "Business", new ObjectMapper()))
                .isInstanceOf(BrevoEmailSender.class);
    }

    @Test
    void usesLoggingSenderWhenNoSettingsAreConfigured() {
        assertThat(configuration.emailSender("", "", "Business", new ObjectMapper()))
                .isInstanceOf(LoggingEmailSender.class);
    }

    @Test
    void rejectsPartialBrevoConfiguration() {
        assertThatThrownBy(() -> configuration.emailSender("key", "", "Business", new ObjectMapper()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must both be configured");
    }
}
