package com.carpenter.business.auth;

public interface SmsSender {
    void send(String phoneNumber, String message);
}
