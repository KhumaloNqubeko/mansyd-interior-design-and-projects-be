package com.carpenter.business.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneVerificationRequest(
        @NotBlank
        @Pattern(regexp = "\\d{10}", message = "must contain exactly 10 digits")
        String phoneNumber) {
}
