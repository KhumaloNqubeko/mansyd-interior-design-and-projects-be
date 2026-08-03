package com.carpenter.business.servicerequest;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
    Page<ServiceRequest> findByCustomerUserId(UUID userId, Pageable pageable);
}
