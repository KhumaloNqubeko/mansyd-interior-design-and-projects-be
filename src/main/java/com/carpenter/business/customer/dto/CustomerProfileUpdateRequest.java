package com.carpenter.business.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerProfileUpdateRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Pattern(regexp = "\\d{10}", message = "must contain exactly 10 digits") String phoneNumber,
        @NotBlank @Size(max = 160) String addressLine1,
        @Size(max = 160) String addressLine2,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 20) String postalCode) {
}
