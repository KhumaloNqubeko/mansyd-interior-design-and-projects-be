package com.carpenter.business.auth;

public interface EmailSender {
    void send(String recipient, String subject, String htmlContent);
}
