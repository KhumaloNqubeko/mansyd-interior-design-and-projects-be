package com.carpenter.business.inventory.dto;

import com.carpenter.business.inventory.UnitOfMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record MaterialRequest(@NotBlank @Size(max = 40) String code,
                              @NotBlank @Size(max = 160) String name,
                              @NotNull UnitOfMeasure unitOfMeasure,
                              @NotNull @DecimalMin("0.00") BigDecimal unitCost,
                              @NotNull @DecimalMin("0.00") BigDecimal reorderLevel,
                              UUID supplierId,
                              boolean active) {
}
