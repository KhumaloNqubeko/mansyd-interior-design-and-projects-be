package com.carpenter.business.project.dto;

import com.carpenter.business.project.ProjectActivity;
import com.carpenter.business.user.Role;
import java.time.Instant;
import java.util.UUID;

public record ProjectActivityResponse(UUID id, UUID projectId, UUID authorId, Role authorRole,
                                      ProjectActivity.Kind kind, String message, String fileName,
                                      String contentType, Instant createdAt) {
    public static ProjectActivityResponse from(ProjectActivity activity) {
        return new ProjectActivityResponse(activity.getId(), activity.getProject().getId(), activity.getAuthor().getId(),
                activity.getAuthor().getRole(), activity.getKind(), activity.getMessage(), activity.getFileName(),
                activity.getContentType(), activity.getCreatedAt());
    }
}
