package com.carpenter.business.quotation.dto;

import com.carpenter.business.quotation.QuotationItemType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record QuotationItemRequest(
        @NotNull QuotationItemType type,
        @NotBlank @Size(max = 180) String description,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantity,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        @NotNull @DecimalMin("0.00") BigDecimal discountAmount,
        @NotNull @DecimalMin("0.00") BigDecimal taxRate) {
}
