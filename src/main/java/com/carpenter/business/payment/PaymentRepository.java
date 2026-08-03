package com.carpenter.business.payment;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findByCustomerUserId(UUID userId, Pageable pageable);
    Page<Payment> findByInvoiceId(UUID invoiceId, Pageable pageable);
}
