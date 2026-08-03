package com.carpenter.business.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceUpdateRequest(@NotNull @FutureOrPresent LocalDate dueDate,
                                   @NotNull @DecimalMin("0.01") BigDecimal totalAmount,
                                   @Size(max = 1000) String notes) {
}
