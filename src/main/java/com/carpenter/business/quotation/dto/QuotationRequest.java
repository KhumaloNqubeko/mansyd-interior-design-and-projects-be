package com.carpenter.business.quotation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record QuotationRequest(
        @NotNull UUID serviceRequestId,
        @NotNull @FutureOrPresent LocalDate expiryDate,
        @Size(max = 1000) String notes) {
}
