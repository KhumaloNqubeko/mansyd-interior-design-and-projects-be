package com.carpenter.business.appointment;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Page<Appointment> findByCustomerUserId(UUID userId, Pageable pageable);
    Page<Appointment> findByProjectId(UUID projectId, Pageable pageable);
    Page<Appointment> findByServiceRequestId(UUID serviceRequestId, Pageable pageable);
}
