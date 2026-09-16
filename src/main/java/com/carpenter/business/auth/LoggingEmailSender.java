package com.carpenter.business.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoggingEmailSender implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String recipient, String subject, String htmlContent) {
        log.info("Development email to {} [{}]: {}", recipient, subject, htmlContent);
    }
}
