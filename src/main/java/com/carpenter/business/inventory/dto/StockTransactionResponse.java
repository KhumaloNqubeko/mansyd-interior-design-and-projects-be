package com.carpenter.business.inventory.dto;

import com.carpenter.business.inventory.StockTransaction;
import com.carpenter.business.inventory.StockTransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockTransactionResponse(UUID id, UUID materialId, String materialCode, UUID projectId,
                                       StockTransactionType type, BigDecimal quantity, String notes,
                                       Instant createdAt) {
    public static StockTransactionResponse from(StockTransaction transaction) {
        return new StockTransactionResponse(transaction.getId(), transaction.getMaterial().getId(),
                transaction.getMaterial().getCode(),
                transaction.getProject() == null ? null : transaction.getProject().getId(),
                transaction.getType(), transaction.getQuantity(), transaction.getNotes(),
                transaction.getCreatedAt());
    }
}
