package com.carpenter.business.quotation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record QuotationUpdateRequest(
        @NotNull @FutureOrPresent LocalDate expiryDate,
        @Size(max = 1000) String notes) {
}
