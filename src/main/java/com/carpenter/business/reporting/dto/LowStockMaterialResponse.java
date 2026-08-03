package com.carpenter.business.reporting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LowStockMaterialResponse(UUID id, String code, String name, BigDecimal stockQuantity,
                                       BigDecimal reorderLevel) {
}
