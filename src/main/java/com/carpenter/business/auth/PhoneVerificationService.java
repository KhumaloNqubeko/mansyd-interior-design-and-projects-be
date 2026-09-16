package com.carpenter.business.auth;

import com.carpenter.business.exception.UnauthorisedOperationException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhoneVerificationService {
    private static final Duration CODE_LIFETIME = Duration.ofMinutes(5);
    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(10);
    private static final Duration RESEND_DELAY = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final Map<String, VerifiedPhone> verifiedPhones = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final SmsSender smsSender;
    private final Clock clock;

    @Autowired
    public PhoneVerificationService(SmsSender smsSender) {
        this(smsSender, Clock.systemUTC());
    }

    PhoneVerificationService(SmsSender smsSender, Clock clock) {
        this.smsSender = smsSender;
        this.clock = clock;
    }

    public void sendCode(String phoneNumber) {
        Instant now = clock.instant();
        Challenge current = challenges.get(phoneNumber);
        if (current != null && current.sentAt().plus(RESEND_DELAY).isAfter(now)) {
            throw new IllegalArgumentException("Please wait before requesting another verification code.");
        }
        String code = "%06d".formatted(random.nextInt(1_000_000));
        challenges.put(phoneNumber, new Challenge(code, now, now.plus(CODE_LIFETIME), 0));
        try {
            smsSender.send(phoneNumber, "Your Carpentry Business verification code is " + code
                    + ". It expires in 5 minutes.");
        } catch (RuntimeException ex) {
            challenges.remove(phoneNumber);
            throw ex;
        }
    }

    public String verifyCode(String phoneNumber, String code) {
        Instant now = clock.instant();
        Challenge challenge = challenges.get(phoneNumber);
        if (challenge == null || challenge.expiresAt().isBefore(now) || challenge.attempts() >= MAX_ATTEMPTS) {
            challenges.remove(phoneNumber);
            throw new UnauthorisedOperationException("The verification code is invalid or has expired.");
        }
        if (!challenge.code().equals(code)) {
            challenges.put(phoneNumber, challenge.withAttempt());
            throw new UnauthorisedOperationException("The verification code is invalid or has expired.");
        }
        challenges.remove(phoneNumber);
        String token = UUID.randomUUID().toString();
        verifiedPhones.put(token, new VerifiedPhone(phoneNumber, now.plus(TOKEN_LIFETIME)));
        return token;
    }

    public void consume(String phoneNumber, String token) {
        VerifiedPhone verified = token == null ? null : verifiedPhones.remove(token);
        if (verified == null || !verified.phoneNumber().equals(phoneNumber)
                || verified.expiresAt().isBefore(clock.instant())) {
            throw new UnauthorisedOperationException("Verify the phone number before creating the account.");
        }
    }

    private record Challenge(String code, Instant sentAt, Instant expiresAt, int attempts) {
        Challenge withAttempt() { return new Challenge(code, sentAt, expiresAt, attempts + 1); }
    }

    private record VerifiedPhone(String phoneNumber, Instant expiresAt) { }
}
