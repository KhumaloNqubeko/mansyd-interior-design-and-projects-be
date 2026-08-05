package com.carpenter.business.project.dto;

import com.carpenter.business.project.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record ProjectStatusUpdateRequest(@NotNull ProjectStatus status) {
}
