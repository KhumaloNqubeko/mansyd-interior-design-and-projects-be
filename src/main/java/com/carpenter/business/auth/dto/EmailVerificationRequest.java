package com.carpenter.business.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmailVerificationRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank
        @Pattern(regexp = "0\\d{9}", message = "must be a 10-digit South African number starting with 0")
        String phoneNumber) {
}
