package com.carpenter.business.notification;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "action_url", nullable = false, length = 240)
    private String actionUrl;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() { }

    public Notification(User recipient, NotificationType type, String title, String message, String actionUrl) {
        this.recipient = recipient;
        this.type = type;
        this.title = title;
        this.message = message;
        this.actionUrl = actionUrl;
    }

    public UUID getId() { return id; }
    public User getRecipient() { return recipient; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getActionUrl() { return actionUrl; }
    public Instant getReadAt() { return readAt; }
    public boolean isRead() { return readAt != null; }

    public void markRead(Instant readAt) {
        if (this.readAt == null) this.readAt = readAt;
    }
}
