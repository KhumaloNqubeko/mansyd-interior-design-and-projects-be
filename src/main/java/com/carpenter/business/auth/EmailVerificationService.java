package com.carpenter.business.auth;

import com.carpenter.business.exception.UnauthorisedOperationException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {
    private static final Duration CODE_LIFETIME = Duration.ofMinutes(5);
    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(10);
    private static final Duration RESEND_DELAY = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final Map<String, VerifiedEmail> verifiedEmails = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final EmailSender emailSender;
    private final Clock clock;

    @Autowired
    public EmailVerificationService(EmailSender emailSender) {
        this(emailSender, Clock.systemUTC());
    }

    EmailVerificationService(EmailSender emailSender, Clock clock) {
        this.emailSender = emailSender;
        this.clock = clock;
    }

    public void sendCode(String suppliedEmail) {
        String email = normalize(suppliedEmail);
        Instant now = clock.instant();
        Challenge current = challenges.get(email);
        if (current != null && current.sentAt().plus(RESEND_DELAY).isAfter(now)) {
            throw new IllegalArgumentException("Please wait before requesting another verification code.");
        }
        String code = "%06d".formatted(random.nextInt(1_000_000));
        challenges.put(email, new Challenge(code, now, now.plus(CODE_LIFETIME), 0));
        String html = "<p>Your Carpentry Business verification code is:</p><h1>" + code
                + "</h1><p>It expires in 5 minutes.</p>";
        try {
            emailSender.send(email, "Verify your Carpentry Business account", html);
        } catch (RuntimeException ex) {
            challenges.remove(email);
            throw ex;
        }
    }

    public String verifyCode(String suppliedEmail, String code) {
        String email = normalize(suppliedEmail);
        Instant now = clock.instant();
        Challenge challenge = challenges.get(email);
        if (challenge == null || challenge.expiresAt().isBefore(now) || challenge.attempts() >= MAX_ATTEMPTS) {
            challenges.remove(email);
            throw new UnauthorisedOperationException("The verification code is invalid or has expired.");
        }
        if (!challenge.code().equals(code)) {
            challenges.put(email, challenge.withAttempt());
            throw new UnauthorisedOperationException("The verification code is invalid or has expired.");
        }
        challenges.remove(email);
        String token = UUID.randomUUID().toString();
        verifiedEmails.put(token, new VerifiedEmail(email, now.plus(TOKEN_LIFETIME)));
        return token;
    }

    public void consume(String suppliedEmail, String token) {
        String email = normalize(suppliedEmail);
        VerifiedEmail verified = token == null ? null : verifiedEmails.remove(token);
        if (verified == null || !verified.email().equals(email) || verified.expiresAt().isBefore(clock.instant())) {
            throw new UnauthorisedOperationException("Verify the email address before creating the account.");
        }
    }

    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }

    private record Challenge(String code, Instant sentAt, Instant expiresAt, int attempts) {
        Challenge withAttempt() { return new Challenge(code, sentAt, expiresAt, attempts + 1); }
    }

    private record VerifiedEmail(String email, Instant expiresAt) { }
}
