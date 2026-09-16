package com.carpenter.business.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
class LoggingSmsSender implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public void send(String phoneNumber, String message) {
        log.info("Development SMS to {}: {}", phoneNumber, message);
    }
}
