package com.carpenter.business.appointment.dto;

import com.carpenter.business.appointment.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusUpdateRequest(@NotNull AppointmentStatus status) {
}
