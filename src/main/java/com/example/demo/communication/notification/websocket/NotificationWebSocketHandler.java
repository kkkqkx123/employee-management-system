package com.example.demo.communication.notification.websocket;

import com.example.demo.communication.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle user subscription to notifications
     */
    @MessageMapping("/notifications/subscribe")
    public void subscribeToNotifications(
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            Long userId = getUserIdFromPrincipal(principal);
            log.info("User {} subscribed to notifications", userId);
            
            // Send current unread count
            long unreadCount = notificationService.getUnreadCount(userId);
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications/count",
                    java.util.Map.of("unreadCount", unreadCount)
            );
            
        } catch (Exception e) {
            log.error("Error handling notification subscription: {}", e.getMessage());
        }
    }

    /**
     * Handle marking notification as read via WebSocket
     */
    @MessageMapping("/notifications/read")
    public void markNotificationAsRead(
            @Payload NotificationReadMessage message,
            Principal principal) {
        
        try {
            Long userId = getUserIdFromPrincipal(principal);
            
            boolean success = notificationService.markAsRead(message.getNotificationId(), userId);
            
            if (success) {
                // Send confirmation back to user
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/notifications/read-confirm",
                        java.util.Map.of(
                                "notificationId", message.getNotificationId(),
                                "success", true
                        )
                );
            }
            
        } catch (Exception e) {
            log.error("Error marking notification as read via WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Handle user presence for notifications
     */
    @MessageMapping("/notifications/presence")
    public void handleNotificationPresence(
            @Payload PresenceMessage message,
            Principal principal) {
        
        try {
            Long userId = getUserIdFromPrincipal(principal);
            log.debug("User {} notification presence: {}", userId, message.isOnline());
            
            // You can implement presence-based notification logic here
            // For example, queue notifications when user is offline
            
        } catch (Exception e) {
            log.error("Error handling notification presence: {}", e.getMessage());
        }
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }

    // Inner classes for WebSocket message payloads
    public static class NotificationReadMessage {
        private Long notificationId;
        
        public NotificationReadMessage() {}
        
        public NotificationReadMessage(Long notificationId) {
            this.notificationId = notificationId;
        }
        
        public Long getNotificationId() { return notificationId; }
        public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    }

    public static class PresenceMessage {
        private boolean online;
        
        public PresenceMessage() {}
        
        public PresenceMessage(boolean online) {
            this.online = online;
        }
        
        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
    }
}