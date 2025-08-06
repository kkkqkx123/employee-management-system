package com.example.demo.communication.notification.dto;

import com.example.demo.communication.notification.entity.NotificationType;
import com.example.demo.communication.notification.entity.NotificationPriority;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private NotificationType type;
    private NotificationPriority priority;
    private Boolean isRead;
    private Instant readAt;
    private Instant createdAt;
    private Long senderId;
    private Long referenceId;
    private String referenceType;
    private String actionUrl;
    private String metadata;
    private Instant expiresAt;
    
    // Additional fields for UI
    private String senderName;
    private String timeAgo;
    private boolean canMarkAsRead;
}