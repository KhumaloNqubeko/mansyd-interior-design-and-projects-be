package com.carpenter.business.project.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ProjectUpdateRequest(LocalDate plannedStartDate, LocalDate plannedCompletionDate,
                                   @Size(max = 1000) String notes) {
}
