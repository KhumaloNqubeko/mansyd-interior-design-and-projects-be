package com.carpenter.business.notification;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.notification.dto.NotificationResponse;
import com.carpenter.business.notification.dto.UnreadNotificationCountResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    PageResponse<NotificationResponse> myNotifications(Authentication authentication,
                                                       @PageableDefault(size = 20) Pageable pageable) {
        return notificationService.myNotifications(authentication, pageable);
    }

    @GetMapping("/unread-count")
    UnreadNotificationCountResponse unreadCount(Authentication authentication) {
        return notificationService.unreadCount(authentication);
    }

    @PostMapping("/{id}/read")
    NotificationResponse markRead(@PathVariable UUID id, Authentication authentication) {
        return notificationService.markRead(id, authentication);
    }
}
