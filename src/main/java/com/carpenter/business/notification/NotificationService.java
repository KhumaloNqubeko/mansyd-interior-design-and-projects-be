package com.carpenter.business.notification;

import com.carpenter.business.common.PageResponse;
import com.carpenter.business.exception.ResourceNotFoundException;
import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.notification.dto.NotificationResponse;
import com.carpenter.business.notification.dto.UnreadNotificationCountResponse;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import com.carpenter.business.user.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                               CurrentUser currentUser) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public void notifyUser(User recipient, NotificationType type, String title, String message, String actionUrl) {
        notificationRepository.save(new Notification(recipient, type, clean(title), clean(message), clean(actionUrl)));
    }

    @Transactional
    public void notifyRole(Role role, NotificationType type, String title, String message, String actionUrl) {
        userRepository.findByRole(role).forEach(user -> notifyUser(user, type, title, message, actionUrl));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> myNotifications(Authentication authentication, Pageable pageable) {
        User user = currentUser.require(authentication);
        return PageResponse.from(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(NotificationResponse::from));
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse unreadCount(Authentication authentication) {
        User user = currentUser.require(authentication);
        return new UnreadNotificationCountResponse(notificationRepository.countByRecipientIdAndReadAtIsNull(user.getId()));
    }

    @Transactional
    public NotificationResponse markRead(UUID id, Authentication authentication) {
        User user = currentUser.require(authentication);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification was not found."));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new UnauthorisedOperationException("You cannot update another user's notification.");
        }
        notification.markRead(Instant.now());
        return NotificationResponse.from(notification);
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
}
