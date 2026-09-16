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
        @Pattern(regexp = "0\\d{9}", message = "must be a 10-digit South African number starting with 0")
        String phoneNumber,
        @NotBlank
        @Size(min = 8, max = 11)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "must include upper-case, lower-case, number and special characters")
        String password,
        @NotBlank String emailVerificationToken,
        @NotNull @Valid AddressRequest address) { }
