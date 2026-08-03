package com.carpenter.business.payment.dto;

import com.carpenter.business.payment.Payment;
import com.carpenter.business.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentResponse(UUID id, UUID invoiceId, String invoiceNumber, UUID customerId, String customerName,
                              BigDecimal amount, LocalDate paymentDate, String proofReference, PaymentStatus status,
                              String notes, Instant createdAt, Instant updatedAt) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getInvoice().getId(), payment.getInvoice().getInvoiceNumber(),
                payment.getCustomer().getId(), payment.getCustomer().getFullName(), payment.getAmount(),
                payment.getPaymentDate(), payment.getProofReference(), payment.getStatus(), payment.getNotes(),
                payment.getCreatedAt(), payment.getUpdatedAt());
    }
}
