package com.carpenter.business.expense.dto;

import com.carpenter.business.expense.Expense;
import com.carpenter.business.expense.ExpenseCategory;
import com.carpenter.business.expense.ExpenseStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(UUID id, String title, ExpenseCategory category, ExpenseStatus status,
                              BigDecimal amount, LocalDate expenseDate, String receiptReference, String notes,
                              UUID supplierId, String supplierName, UUID projectId, String projectNumber,
                              UUID materialId, String materialCode, String createdByEmail, Instant createdAt) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(expense.getId(), expense.getTitle(), expense.getCategory(), expense.getStatus(),
                expense.getAmount(), expense.getExpenseDate(), expense.getReceiptReference(), expense.getNotes(),
                expense.getSupplier() == null ? null : expense.getSupplier().getId(),
                expense.getSupplier() == null ? null : expense.getSupplier().getName(),
                expense.getProject() == null ? null : expense.getProject().getId(),
                expense.getProject() == null ? null : expense.getProject().getProjectNumber(),
                expense.getMaterial() == null ? null : expense.getMaterial().getId(),
                expense.getMaterial() == null ? null : expense.getMaterial().getCode(),
                expense.getCreatedBy().getEmail(), expense.getCreatedAt());
    }
}
