package com.carpenter.business.appointment.dto;

import com.carpenter.business.appointment.Appointment;
import com.carpenter.business.appointment.AppointmentStatus;
import com.carpenter.business.appointment.AppointmentType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(UUID id, String title, AppointmentType type, AppointmentStatus status,
                                  LocalDateTime scheduledStart, LocalDateTime scheduledEnd, String location,
                                  String notes, UUID customerId, String customerName, UUID serviceRequestId,
                                  String serviceRequestTitle, UUID projectId, String projectNumber,
                                  String createdByEmail, Instant createdAt, Instant updatedAt) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getTitle(), appointment.getType(),
                appointment.getStatus(), appointment.getScheduledStart(), appointment.getScheduledEnd(),
                appointment.getLocation(), appointment.getNotes(), appointment.getCustomer().getId(),
                appointment.getCustomer().getFullName(),
                appointment.getServiceRequest() == null ? null : appointment.getServiceRequest().getId(),
                appointment.getServiceRequest() == null ? null : appointment.getServiceRequest().getTitle(),
                appointment.getProject() == null ? null : appointment.getProject().getId(),
                appointment.getProject() == null ? null : appointment.getProject().getProjectNumber(),
                appointment.getCreatedBy().getEmail(), appointment.getCreatedAt(), appointment.getUpdatedAt());
    }
}
