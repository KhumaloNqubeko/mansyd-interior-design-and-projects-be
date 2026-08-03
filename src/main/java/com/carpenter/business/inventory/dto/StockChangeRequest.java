package com.carpenter.business.inventory.dto;

import com.carpenter.business.inventory.StockTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record StockChangeRequest(@NotNull UUID materialId,
                                 UUID projectId,
                                 @NotNull StockTransactionType type,
                                 @NotNull @DecimalMin("0.001") BigDecimal quantity,
                                 @Size(max = 500) String notes) {
}
