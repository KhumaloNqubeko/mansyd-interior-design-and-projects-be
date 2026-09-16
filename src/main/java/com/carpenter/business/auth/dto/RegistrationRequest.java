package com.carpenter.business.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank
        @Pattern(regexp = "\\d{10}", message = "must contain exactly 10 digits")
        String phoneNumber,
        @NotBlank
        @Size(min = 8, max = 11)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "must include upper-case, lower-case, number and special characters")
        String password,
        @NotBlank String phoneVerificationToken,
        @NotNull @Valid AddressRequest address) { }
