package com.carpenter.business.appointment.dto;

import com.carpenter.business.appointment.AppointmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentRequest(@NotBlank @Size(max = 160) String title,
                                 @NotNull AppointmentType type,
                                 @NotNull LocalDateTime scheduledStart,
                                 @NotNull LocalDateTime scheduledEnd,
                                 @NotBlank @Size(max = 300) String location,
                                 @Size(max = 1000) String notes,
                                 @NotNull UUID customerId,
                                 UUID serviceRequestId,
                                 UUID projectId) {
}
