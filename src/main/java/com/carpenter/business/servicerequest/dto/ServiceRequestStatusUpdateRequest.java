package com.carpenter.business.servicerequest.dto;

import com.carpenter.business.servicerequest.ServiceRequestStatus;
import jakarta.validation.constraints.NotNull;

public record ServiceRequestStatusUpdateRequest(@NotNull ServiceRequestStatus status) {
}
