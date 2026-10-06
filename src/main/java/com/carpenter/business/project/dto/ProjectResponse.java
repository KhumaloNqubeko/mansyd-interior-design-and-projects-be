package com.carpenter.business.project.dto;

import com.carpenter.business.project.Project;
import com.carpenter.business.project.ProjectStatus;
import com.carpenter.business.project.CompletionReviewStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(UUID id, String projectNumber, UUID orderId, String orderNumber, UUID customerId,
                              String customerName, ProjectStatus status, int progress, LocalDate plannedStartDate,
                              LocalDate plannedCompletionDate, LocalDate actualStartDate,
                              LocalDate actualCompletionDate, String notes, Instant createdAt, Instant updatedAt,
                              CompletionReviewStatus completionReviewStatus, UUID completionReviewId,
                              Instant customerConfirmedAt, UUID customerConfirmedBy) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.getId(), project.getProjectNumber(), project.getOrder().getId(),
                project.getOrder().getOrderNumber(), project.getCustomer().getId(), project.getCustomer().getFullName(),
                project.getStatus(), project.getProgress(), project.getPlannedStartDate(),
                project.getPlannedCompletionDate(), project.getActualStartDate(), project.getActualCompletionDate(),
                project.getNotes(), project.getCreatedAt(), project.getUpdatedAt(), project.getCompletionReviewStatus(),
                project.getCompletionReviewId(), project.getCustomerConfirmedAt(), project.getCustomerConfirmedBy());
    }
}
