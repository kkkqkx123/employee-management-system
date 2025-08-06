package com.example.demo.communication.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class NotificationMarkReadRequest {
    @NotNull(message = "Notification ID is required")
    private Long notificationId;
    
    private List<Long> notificationIds; // For bulk mark as read
    
    private boolean markAll = false; // Mark all notifications as read
}