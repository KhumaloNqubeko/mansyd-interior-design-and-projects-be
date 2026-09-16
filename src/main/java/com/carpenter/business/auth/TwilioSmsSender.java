package com.carpenter.business.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
class TwilioSmsSender implements SmsSender {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    TwilioSmsSender(@Value("${app.sms.twilio.account-sid}") String accountSid,
                    @Value("${app.sms.twilio.auth-token}") String authToken,
                    @Value("${app.sms.twilio.from-number}") String fromNumber) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
    }

    @Override
    public void send(String phoneNumber, String message) {
        String body = "To=" + encode(toSouthAfricanE164(phoneNumber))
                + "&From=" + encode(fromNumber) + "&Body=" + encode(message);
        String credentials = Base64.getEncoder().encodeToString(
                (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("The verification SMS could not be sent.");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("The verification SMS could not be sent.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("The verification SMS could not be sent.", ex);
        }
    }

    private String toSouthAfricanE164(String phoneNumber) {
        return "+27" + phoneNumber.substring(1);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
