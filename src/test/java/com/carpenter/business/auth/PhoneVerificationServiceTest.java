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

class PhoneVerificationServiceTest {
    private static final String PHONE = "0123456789";

    @Test
    void verifiedTokenIsBoundToPhoneAndCanOnlyBeConsumedOnce() {
        CapturingSmsSender sender = new CapturingSmsSender();
        PhoneVerificationService service = new PhoneVerificationService(sender,
                Clock.fixed(Instant.parse("2026-09-16T09:00:00Z"), ZoneOffset.UTC));

        service.sendCode(PHONE);
        String token = service.verifyCode(PHONE, sender.code());
        service.consume(PHONE, token);

        assertThat(token).isNotBlank();
        assertThatThrownBy(() -> service.consume(PHONE, token))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void incorrectCodeIsRejected() {
        CapturingSmsSender sender = new CapturingSmsSender();
        PhoneVerificationService service = new PhoneVerificationService(sender);
        service.sendCode(PHONE);

        assertThatThrownBy(() -> service.verifyCode(PHONE, "999999".equals(sender.code()) ? "000000" : "999999"))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void springCanConstructServiceWithSmsDependency() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(SmsSender.class, CapturingSmsSender::new);
            context.register(PhoneVerificationService.class);
            context.refresh();

            assertThat(context.getBean(PhoneVerificationService.class)).isNotNull();
        }
    }

    private static class CapturingSmsSender implements SmsSender {
        private String message;

        @Override
        public void send(String phoneNumber, String message) {
            this.message = message;
        }

        String code() {
            Matcher matcher = Pattern.compile("\\b(\\d{6})\\b").matcher(message);
            if (!matcher.find()) throw new AssertionError("SMS did not contain a six-digit code");
            return matcher.group(1);
        }
    }
}
