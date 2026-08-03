package com.carpenter.business.project;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByOrderId(UUID orderId);
    Page<Project> findByCustomerUserId(UUID userId, Pageable pageable);
}
