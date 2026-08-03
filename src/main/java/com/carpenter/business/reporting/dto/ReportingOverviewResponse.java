package com.carpenter.business.reporting.dto;

import java.util.List;
import java.util.Map;

public record ReportingOverviewResponse(FinancialReportResponse financial,
                                        Map<String, Long> ordersByStatus,
                                        Map<String, Long> projectsByStatus,
                                        Map<String, Long> invoicesByStatus,
                                        Map<String, Long> paymentsByStatus,
                                        Map<String, Long> expensesByCategory,
                                        InventoryReportResponse inventory,
                                        List<LowStockMaterialResponse> lowStockMaterials) {
}
