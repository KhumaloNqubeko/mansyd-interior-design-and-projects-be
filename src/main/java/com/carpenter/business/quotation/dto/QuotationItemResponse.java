package com.carpenter.business.quotation.dto;

import com.carpenter.business.quotation.QuotationItem;
import com.carpenter.business.quotation.QuotationItemType;
import java.math.BigDecimal;
import java.util.UUID;

public record QuotationItemResponse(UUID id, QuotationItemType type, String description, BigDecimal quantity,
                                    BigDecimal unitPrice, BigDecimal discountAmount, BigDecimal taxRate,
                                    BigDecimal lineTotal) {
    public static QuotationItemResponse from(QuotationItem item) {
        return new QuotationItemResponse(item.getId(), item.getType(), item.getDescription(), item.getQuantity(),
                item.getUnitPrice(), item.getDiscountAmount(), item.getTaxRate(), item.getLineTotal());
    }
}
