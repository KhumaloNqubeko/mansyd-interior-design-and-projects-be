package com.carpenter.business.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carpenter.business.exception.UnauthorisedOperationException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class EmailVerificationServiceTest {
    private static final String EMAIL = "customer@example.com";

    @Test
    void verifiedTokenIsBoundToNormalizedEmailAndCanOnlyBeConsumedOnce() {
        CapturingEmailSender sender = new CapturingEmailSender();
        EmailVerificationService service = new EmailVerificationService(sender,
                Clock.fixed(Instant.parse("2026-09-16T09:00:00Z"), ZoneOffset.UTC));

        service.sendCode(" Customer@Example.COM ");
        assertThat(sender.recipient()).isEqualTo(EMAIL);
        String token = service.verifyCode(EMAIL, sender.code());
        service.consume(" CUSTOMER@example.com ", token);

        assertThat(token).isNotBlank();
        assertThatThrownBy(() -> service.consume(EMAIL, token))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void incorrectCodeIsRejected() {
        CapturingEmailSender sender = new CapturingEmailSender();
        EmailVerificationService service = new EmailVerificationService(sender);
        service.sendCode(EMAIL);

        assertThatThrownBy(() -> service.verifyCode(EMAIL,
                "999999".equals(sender.code()) ? "000000" : "999999"))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void springCanConstructServiceWithEmailDependency() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(EmailSender.class, CapturingEmailSender::new);
            context.register(EmailVerificationService.class);
            context.refresh();
            assertThat(context.getBean(EmailVerificationService.class)).isNotNull();
        }
    }

    private static class CapturingEmailSender implements EmailSender {
        private String recipient;
        private String content;

        @Override
        public void send(String recipient, String subject, String htmlContent) {
            this.recipient = recipient;
            this.content = htmlContent;
        }

        String recipient() { return recipient; }

        String code() {
            Matcher matcher = Pattern.compile("\\b(\\d{6})\\b").matcher(content);
            if (!matcher.find()) throw new AssertionError("Email did not contain a six-digit code");
            return matcher.group(1);
        }
    }
}
