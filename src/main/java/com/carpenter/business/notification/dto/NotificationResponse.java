package com.carpenter.business.notification.dto;

import com.carpenter.business.notification.Notification;
import com.carpenter.business.notification.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, NotificationType type, String title, String message, String actionUrl,
                                   boolean read, Instant readAt, Instant createdAt) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.getActionUrl(), notification.isRead(),
                notification.getReadAt(), notification.getCreatedAt());
    }
}
