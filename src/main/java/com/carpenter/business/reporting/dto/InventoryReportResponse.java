package com.carpenter.business.reporting.dto;

import java.math.BigDecimal;

public record InventoryReportResponse(long materialCount, long lowStockCount, BigDecimal stockValue) {
}
