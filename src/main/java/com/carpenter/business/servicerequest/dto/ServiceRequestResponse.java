package com.carpenter.business.servicerequest.dto;

import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.servicerequest.ServiceRequestStatus;
import java.time.Instant;
import java.util.UUID;

public record ServiceRequestResponse(UUID id, UUID customerId, String customerName, String title, String description,
                                     String preferredContactMethod, String siteAddress, ServiceRequestStatus status,
                                     Instant createdAt, Instant updatedAt) {
    public static ServiceRequestResponse from(ServiceRequest request) {
        return new ServiceRequestResponse(request.getId(), request.getCustomer().getId(),
                request.getCustomer().getFullName(), request.getTitle(), request.getDescription(),
                request.getPreferredContactMethod(), request.getSiteAddress(), request.getStatus(),
                request.getCreatedAt(), request.getUpdatedAt());
    }
}
