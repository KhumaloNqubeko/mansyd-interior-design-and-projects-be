package com.carpenter.business.quotation;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationRepository extends JpaRepository<Quotation, UUID> {
    boolean existsByServiceRequestId(UUID serviceRequestId);
    Page<Quotation> findByCustomerUserId(UUID userId, Pageable pageable);
    Optional<Quotation> findByIdAndCustomerUserId(UUID id, UUID userId);
}
