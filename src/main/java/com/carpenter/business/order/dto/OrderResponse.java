package com.carpenter.business.order.dto;

import com.carpenter.business.order.Order;
import com.carpenter.business.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(UUID id, String orderNumber, UUID quotationId, String quotationNumber, UUID customerId,
                            String customerName, BigDecimal acceptedTotal, OrderStatus status,
                            Instant createdAt, Instant updatedAt) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getOrderNumber(), order.getQuotation().getId(),
                order.getQuotation().getQuotationNumber(), order.getCustomer().getId(),
                order.getCustomer().getFullName(), order.getAcceptedTotal(), order.getStatus(),
                order.getCreatedAt(), order.getUpdatedAt());
    }
}
