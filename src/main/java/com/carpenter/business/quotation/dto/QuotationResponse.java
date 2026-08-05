package com.carpenter.business.quotation.dto;

import com.carpenter.business.quotation.Quotation;
import com.carpenter.business.quotation.QuotationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuotationResponse(UUID id, String quotationNumber, UUID serviceRequestId, UUID customerId,
                                String customerName, QuotationStatus status, LocalDate expiryDate, String notes,
                                String rejectionNotes, BigDecimal subtotal, BigDecimal discountTotal,
                                BigDecimal taxTotal, BigDecimal total, List<QuotationItemResponse> items,
                                UUID orderId, Instant createdAt, Instant updatedAt) {
    public static QuotationResponse from(Quotation quotation, UUID orderId) {
        return new QuotationResponse(quotation.getId(), quotation.getQuotationNumber(),
                quotation.getServiceRequest().getId(), quotation.getCustomer().getId(),
                quotation.getCustomer().getFullName(), quotation.getStatus(), quotation.getExpiryDate(),
                quotation.getNotes(), quotation.getRejectionNotes(), quotation.getSubtotal(),
                quotation.getDiscountTotal(), quotation.getTaxTotal(), quotation.getTotal(),
                quotation.getItems().stream().map(QuotationItemResponse::from).toList(), orderId,
                quotation.getCreatedAt(), quotation.getUpdatedAt());
    }
}
