package com.carpenter.business.servicerequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceRequestCreateRequest(
        @NotBlank @Size(max = 140) String title,
        @NotBlank @Size(max = 4000) String description,
        @NotBlank @Size(max = 30) String preferredContactMethod,
        @NotBlank @Size(max = 300) String siteAddress) {
}
