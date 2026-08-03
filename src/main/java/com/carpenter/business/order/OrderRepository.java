package com.carpenter.business.order;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByQuotationId(UUID quotationId);
    Page<Order> findByCustomerUserId(UUID userId, Pageable pageable);
}
