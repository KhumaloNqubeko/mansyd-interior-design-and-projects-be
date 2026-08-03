package com.carpenter.business.invoice.dto;

import com.carpenter.business.invoice.Invoice;
import com.carpenter.business.invoice.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponse(UUID id, String invoiceNumber, UUID orderId, String orderNumber, UUID customerId,
                              String customerName, InvoiceStatus status, LocalDate issueDate, LocalDate dueDate,
                              BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal balanceDue, String notes,
                              Instant createdAt, Instant updatedAt) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getOrder().getId(),
                invoice.getOrder().getOrderNumber(), invoice.getCustomer().getId(), invoice.getCustomer().getFullName(),
                invoice.getStatus(), invoice.getIssueDate(), invoice.getDueDate(), invoice.getTotalAmount(),
                invoice.getPaidAmount(), invoice.getBalanceDue(), invoice.getNotes(), invoice.getCreatedAt(),
                invoice.getUpdatedAt());
    }
}
