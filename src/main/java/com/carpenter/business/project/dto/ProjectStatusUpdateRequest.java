package com.carpenter.business.project.dto;

import com.carpenter.business.project.ProjectStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProjectStatusUpdateRequest(@NotNull ProjectStatus status, @Min(0) @Max(100) int progress,
                                         LocalDate actualCompletionDate) {
}
