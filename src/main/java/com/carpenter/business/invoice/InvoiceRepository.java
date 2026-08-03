package com.carpenter.business.invoice;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    boolean existsByOrderId(UUID orderId);
    Page<Invoice> findByCustomerUserId(UUID userId, Pageable pageable);
    Optional<Invoice> findByIdAndCustomerUserId(UUID id, UUID userId);
}
