package com.carpenter.business.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentRequest(@NotNull UUID invoiceId,
                             @NotNull @DecimalMin("0.01") BigDecimal amount,
                             @NotNull @PastOrPresent LocalDate paymentDate,
                             @NotBlank @Size(max = 180) String proofReference,
                             @Size(max = 1000) String notes) {
}
