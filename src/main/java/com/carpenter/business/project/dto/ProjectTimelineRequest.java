package com.carpenter.business.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectTimelineRequest(@NotBlank @Size(max = 140) String title,
                                     @NotBlank @Size(max = 2000) String message) {
}
