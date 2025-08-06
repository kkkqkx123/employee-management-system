package com.example.demo.communication.notification.dto;

import com.example.demo.communication.notification.entity.NotificationType;
import com.example.demo.communication.notification.entity.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class NotificationCreateRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private List<Long> userIds; // For bulk notifications
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Type is required")
    private NotificationType type;
    
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    private Long referenceId;
    
    private String referenceType;
    
    @Size(max = 500, message = "Action URL must not exceed 500 characters")
    private String actionUrl;
    
    private String metadata;
    
    private Instant expiresAt;
}