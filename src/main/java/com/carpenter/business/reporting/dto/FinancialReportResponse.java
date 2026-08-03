package com.carpenter.business.reporting.dto;

import java.math.BigDecimal;

public record FinancialReportResponse(BigDecimal acceptedOrderValue, BigDecimal invoicedTotal,
                                      BigDecimal paidTotal, BigDecimal receivablesTotal,
                                      BigDecimal approvedExpenseTotal, BigDecimal netCashPosition) {
}
