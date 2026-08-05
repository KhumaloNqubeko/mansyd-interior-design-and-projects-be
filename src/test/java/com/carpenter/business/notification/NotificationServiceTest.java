package com.carpenter.business.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.carpenter.business.exception.UnauthorisedOperationException;
import com.carpenter.business.security.CurrentUser;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import com.carpenter.business.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationRepository notifications;
    @Mock UserRepository users;
    @Mock CurrentUser currentUser;
    @Mock Authentication authentication;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(notifications, users, currentUser);
    }

    @Test
    void cannotMarkAnotherUsersNotificationRead() {
        User owner = user("owner@example.com");
        User other = user("other@example.com");
        Notification notification = new Notification(owner, NotificationType.GENERAL, "Hello", "Message", "");
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        when(currentUser.require(authentication)).thenReturn(other);
        when(notifications.findById(notification.getId())).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.markRead(notification.getId(), authentication))
                .isInstanceOf(UnauthorisedOperationException.class);
    }

    @Test
    void ownerCanMarkNotificationRead() {
        User owner = user("owner@example.com");
        Notification notification = new Notification(owner, NotificationType.GENERAL, "Hello", "Message", "");
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        when(currentUser.require(authentication)).thenReturn(owner);
        when(notifications.findById(notification.getId())).thenReturn(Optional.of(notification));

        assertThat(service.markRead(notification.getId(), authentication).read()).isTrue();
    }

    private User user(String email) {
        User user = new User(email, "hash", Role.CUSTOMER, AccountStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
