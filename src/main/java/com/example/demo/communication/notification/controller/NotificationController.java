package com.example.demo.communication.notification.controller;

import com.example.demo.communication.notification.dto.NotificationDto;
import com.example.demo.communication.notification.dto.NotificationCreateRequest;
import com.example.demo.communication.notification.dto.NotificationMarkReadRequest;
import com.example.demo.communication.notification.entity.NotificationType;
import com.example.demo.communication.notification.service.NotificationService;
import com.example.demo.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_SEND')")
    public ResponseEntity<ApiResponse<NotificationDto>> createNotification(
            @Valid @RequestBody NotificationCreateRequest request,
            Authentication authentication) {
        
        Long senderId = getCurrentUserId(authentication);
        NotificationDto notification = notificationService.createNotification(request, senderId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(notification, "Notification created successfully"));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('NOTIFICATION_SEND')")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> createBulkNotifications(
            @Valid @RequestBody NotificationCreateRequest request,
            Authentication authentication) {
        
        Long senderId = getCurrentUserId(authentication);
        List<NotificationDto> notifications = notificationService.createBulkNotifications(request, senderId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(notifications, "Bulk notifications created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getUserNotifications(
            Pageable pageable,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Page<NotificationDto> notifications = notificationService.getUserNotifications(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    @GetMapping("/unread")
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getUnreadNotifications(
            Pageable pageable,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Page<NotificationDto> notifications = notificationService.getUnreadNotifications(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotificationsByType(
            @PathVariable NotificationType type,
            Pageable pageable,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Page<NotificationDto> notifications = notificationService.getNotificationsByType(userId, type, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        boolean success = notificationService.markAsRead(id, userId);
        
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to mark notification as read"));
        }
    }

    @PutMapping("/read")
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<Integer>> markMultipleAsRead(
            @Valid @RequestBody NotificationMarkReadRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        
        if (request.isMarkAll()) {
            int count = notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(ApiResponse.success(count, "All notifications marked as read"));
        } else if (request.getNotificationIds() != null && !request.getNotificationIds().isEmpty()) {
            int count = notificationService.markMultipleAsRead(request.getNotificationIds(), userId);
            return ResponseEntity.ok(ApiResponse.success(count, "Notifications marked as read"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request: no notifications specified"));
        }
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        long count = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/unread-count/{type}")
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCountByType(
            @PathVariable NotificationType type,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        long count = notificationService.getUnreadCountByType(userId, type);
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        boolean success = notificationService.deleteNotification(id, userId);
        
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete notification"));
        }
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getRecentNotifications(
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        List<NotificationDto> notifications = notificationService.getRecentNotifications(userId);
        
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    private Long getCurrentUserId(Authentication authentication) {
        // This should extract user ID from the authentication object
        // Implementation depends on your security setup
        return Long.parseLong(authentication.getName());
    }
}