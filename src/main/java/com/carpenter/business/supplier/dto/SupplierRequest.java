package com.carpenter.business.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(@NotBlank @Size(max = 140) String name,
                              @Size(max = 160) String contactName,
                              @Email @Size(max = 254) String email,
                              @Size(max = 30) String phoneNumber,
                              boolean active) {
}
