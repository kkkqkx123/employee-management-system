package com.example.demo.communication.notification.service;

import com.example.demo.communication.notification.dto.NotificationDto;
import com.example.demo.communication.notification.dto.NotificationCreateRequest;
import com.example.demo.communication.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationService {
    
    /**
     * Create a single notification
     * @param request Notification creation request
     * @param senderId User ID who is sending the notification
     * @return Created notification DTO
     */
    NotificationDto createNotification(NotificationCreateRequest request, Long senderId);
    
    /**
     * Create bulk notifications
     * @param request Notification creation request with multiple user IDs
     * @param senderId User ID who is sending the notification
     * @return List of created notification DTOs
     */
    List<NotificationDto> createBulkNotifications(NotificationCreateRequest request, Long senderId);
    
    /**
     * Send notification asynchronously with real-time delivery
     * @param userId Recipient user ID
     * @param title Notification title
     * @param content Notification content
     * @param type Notification type
     * @param senderId Sender user ID (optional)
     * @return CompletableFuture for async processing
     */
    CompletableFuture<NotificationDto> sendNotificationAsync(Long userId, String title, String content, 
                                                            NotificationType type, Long senderId);
    
    /**
     * Get notifications for a user
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of notification DTOs
     */
    Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable);
    
    /**
     * Get unread notifications for a user
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of unread notification DTOs
     */
    Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable);
    
    /**
     * Get notifications by type for a user
     * @param userId User ID
     * @param type Notification type
     * @param pageable Pagination parameters
     * @return Page of notification DTOs
     */
    Page<NotificationDto> getNotificationsByType(Long userId, NotificationType type, Pageable pageable);
    
    /**
     * Mark notification as read
     * @param notificationId Notification ID
     * @param userId User ID
     * @return true if successfully marked as read
     */
    boolean markAsRead(Long notificationId, Long userId);
    
    /**
     * Mark multiple notifications as read
     * @param notificationIds List of notification IDs
     * @param userId User ID
     * @return Number of notifications marked as read
     */
    int markMultipleAsRead(List<Long> notificationIds, Long userId);
    
    /**
     * Mark all notifications as read for a user
     * @param userId User ID
     * @return Number of notifications marked as read
     */
    int markAllAsRead(Long userId);
    
    /**
     * Get unread notification count for a user
     * @param userId User ID
     * @return Unread notification count
     */
    long getUnreadCount(Long userId);
    
    /**
     * Get unread notification count by type for a user
     * @param userId User ID
     * @param type Notification type
     * @return Unread notification count for the type
     */
    long getUnreadCountByType(Long userId, NotificationType type);
    
    /**
     * Delete notification
     * @param notificationId Notification ID
     * @param userId User ID (must be the recipient)
     * @return true if successfully deleted
     */
    boolean deleteNotification(Long notificationId, Long userId);
    
    /**
     * Clean up expired notifications
     * @return Number of expired notifications deleted
     */
    int cleanupExpiredNotifications();
    
    /**
     * Get recent notifications for a user (last 24 hours)
     * @param userId User ID
     * @return List of recent notification DTOs
     */
    List<NotificationDto> getRecentNotifications(Long userId);
    
    /**
     * Send system notification to user
     * @param userId User ID
     * @param title Notification title
     * @param content Notification content
     * @return CompletableFuture for async processing
     */
    CompletableFuture<NotificationDto> sendSystemNotification(Long userId, String title, String content);
    
    /**
     * Send announcement notification to multiple users
     * @param userIds List of user IDs
     * @param title Notification title
     * @param content Notification content
     * @param announcementId Reference to announcement
     * @param senderId Sender user ID
     * @return CompletableFuture for async processing
     */
    CompletableFuture<List<NotificationDto>> sendAnnouncementNotifications(List<Long> userIds, String title, 
                                                                          String content, Long announcementId, Long senderId);
}