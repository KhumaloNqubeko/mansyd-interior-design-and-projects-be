package com.carpenter.business.project.dto;

import com.carpenter.business.project.ProjectUpdate;
import com.carpenter.business.user.Role;
import java.time.Instant;
import java.util.UUID;

public record ProjectTimelineResponse(UUID id, UUID projectId, UUID createdByUserId, Role createdByRole,
                                      String title, String message, Instant createdAt) {
    public static ProjectTimelineResponse from(ProjectUpdate update) {
        return new ProjectTimelineResponse(update.getId(), update.getProject().getId(),
                update.getCreatedBy().getId(), update.getCreatedBy().getRole(),
                update.getTitle(), update.getMessage(), update.getCreatedAt());
    }
}
