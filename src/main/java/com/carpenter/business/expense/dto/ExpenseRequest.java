package com.carpenter.business.expense.dto;

import com.carpenter.business.expense.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseRequest(@NotBlank @Size(max = 160) String title,
                             @NotNull ExpenseCategory category,
                             @NotNull @DecimalMin("0.01") BigDecimal amount,
                             @NotNull LocalDate expenseDate,
                             @Size(max = 180) String receiptReference,
                             @Size(max = 1000) String notes,
                             UUID supplierId,
                             UUID projectId,
                             UUID materialId) {
}
