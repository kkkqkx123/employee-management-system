package com.example.demo.communication.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_is_read", columnList = "is_read"),
    @Index(name = "idx_notification_created_at", columnList = "created_at"),
    @Index(name = "idx_notification_user_read", columnList = "user_id, is_read")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // Recipient user ID
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private Instant readAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "sender_id")
    private Long senderId; // User who sent the notification (optional)
    
    @Column(name = "reference_id")
    private Long referenceId; // Reference to related entity (e.g., chat message ID, announcement ID)
    
    @Column(name = "reference_type", length = 50)
    private String referenceType; // Type of referenced entity (e.g., "CHAT_MESSAGE", "ANNOUNCEMENT")
    
    @Column(name = "action_url", length = 500)
    private String actionUrl; // URL to navigate when notification is clicked
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data
    
    @Column(name = "expires_at")
    private Instant expiresAt; // Optional expiration time for the notification
}