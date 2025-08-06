package com.example.demo.communication.notification.service.impl;

import com.example.demo.communication.notification.dto.NotificationDto;
import com.example.demo.communication.notification.dto.NotificationCreateRequest;
import com.example.demo.communication.notification.entity.Notification;
import com.example.demo.communication.notification.entity.NotificationType;
import com.example.demo.communication.notification.entity.NotificationPriority;
import com.example.demo.communication.notification.repository.NotificationRepository;
import com.example.demo.communication.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public NotificationDto createNotification(NotificationCreateRequest request, Long senderId) {
        log.info("Creating notification for user {}: {}", request.getUserId(), request.getTitle());
        
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .priority(request.getPriority())
                .senderId(senderId)
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .actionUrl(request.getActionUrl())
                .metadata(request.getMetadata())
                .expiresAt(request.getExpiresAt())
                .isRead(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        NotificationDto notificationDto = convertToDto(savedNotification);
        
        // Send real-time notification via WebSocket
        sendRealTimeNotification(notificationDto);
        
        // Cache notification count in Redis
        updateUnreadCountCache(request.getUserId());
        
        log.info("Notification created successfully: {}", savedNotification.getId());
        return notificationDto;
    }

    @Override
    @Transactional
    public List<NotificationDto> createBulkNotifications(NotificationCreateRequest request, Long senderId) {
        log.info("Creating bulk notifications for {} users: {}", request.getUserIds().size(), request.getTitle());
        
        List<Notification> notifications = request.getUserIds().stream()
                .map(userId -> Notification.builder()
                        .userId(userId)
                        .title(request.getTitle())
                        .content(request.getContent())
                        .type(request.getType())
                        .priority(request.getPriority())
                        .senderId(senderId)
                        .referenceId(request.getReferenceId())
                        .referenceType(request.getReferenceType())
                        .actionUrl(request.getActionUrl())
                        .metadata(request.getMetadata())
                        .expiresAt(request.getExpiresAt())
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());
        
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        List<NotificationDto> notificationDtos = savedNotifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Send real-time notifications
        notificationDtos.forEach(this::sendRealTimeNotification);
        
        // Update cache for all users
        request.getUserIds().forEach(this::updateUnreadCountCache);
        
        log.info("Bulk notifications created successfully: {} notifications", savedNotifications.size());
        return notificationDtos;
    }

    @Override
    @Async
    public CompletableFuture<NotificationDto> sendNotificationAsync(Long userId, String title, String content, 
                                                                   NotificationType type, Long senderId) {
        try {
            NotificationCreateRequest request = new NotificationCreateRequest();
            request.setUserId(userId);
            request.setTitle(title);
            request.setContent(content);
            request.setType(type);
            request.setPriority(NotificationPriority.NORMAL);
            
            NotificationDto notification = createNotification(request, senderId);
            return CompletableFuture.completedFuture(notification);
            
        } catch (Exception e) {
            log.error("Failed to send notification async to user {}: {}", userId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDto);
    }

    @Override
    public Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDto);
    }

    @Override
    public Page<NotificationDto> getNotificationsByType(Long userId, NotificationType type, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        return notifications.map(this::convertToDto);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId, Instant.now());
        
        if (updated > 0) {
            updateUnreadCountCache(userId);
            
            // Send real-time update
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications/read",
                    java.util.Map.of("notificationId", notificationId, "isRead", true)
            );
            
            log.debug("Notification {} marked as read for user {}", notificationId, userId);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional
    public int markMultipleAsRead(List<Long> notificationIds, Long userId) {
        int totalUpdated = 0;
        
        for (Long notificationId : notificationIds) {
            if (markAsRead(notificationId, userId)) {
                totalUpdated++;
            }
        }
        
        log.info("Marked {} notifications as read for user {}", totalUpdated, userId);
        return totalUpdated;
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsRead(userId, Instant.now());
        
        if (updated > 0) {
            updateUnreadCountCache(userId);
            
            // Send real-time update
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications/read-all",
                    java.util.Map.of("count", updated)
            );
            
            log.info("Marked all {} notifications as read for user {}", updated, userId);
        }
        
        return updated;
    }

    @Override
    public long getUnreadCount(Long userId) {
        // Try to get from cache first
        String cacheKey = "notification:unread:" + userId;
        Long cachedCount = (Long) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedCount != null) {
            return cachedCount;
        }
        
        // If not in cache, get from database and cache it
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        redisTemplate.opsForValue().set(cacheKey, count, java.time.Duration.ofMinutes(5));
        
        return count;
    }

    @Override
    public long getUnreadCountByType(Long userId, NotificationType type) {
        return notificationRepository.countByUserIdAndTypeAndIsReadFalse(userId, type);
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        return notificationRepository.findById(notificationId)
                .filter(notification -> notification.getUserId().equals(userId))
                .map(notification -> {
                    notificationRepository.delete(notification);
                    updateUnreadCountCache(userId);
                    log.debug("Notification {} deleted for user {}", notificationId, userId);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public int cleanupExpiredNotifications() {
        int deleted = notificationRepository.deleteExpiredNotifications(Instant.now());
        log.info("Cleaned up {} expired notifications", deleted);
        return deleted;
    }

    @Override
    public List<NotificationDto> getRecentNotifications(Long userId) {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        List<Notification> notifications = notificationRepository.findRecentNotifications(userId, since);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Async
    public CompletableFuture<NotificationDto> sendSystemNotification(Long userId, String title, String content) {
        return sendNotificationAsync(userId, title, content, NotificationType.SYSTEM, null);
    }

    @Override
    @Async
    public CompletableFuture<List<NotificationDto>> sendAnnouncementNotifications(List<Long> userIds, String title, 
                                                                                 String content, Long announcementId, Long senderId) {
        try {
            NotificationCreateRequest request = new NotificationCreateRequest();
            request.setUserIds(userIds);
            request.setTitle(title);
            request.setContent(content);
            request.setType(NotificationType.ANNOUNCEMENT);
            request.setPriority(NotificationPriority.NORMAL);
            request.setReferenceId(announcementId);
            request.setReferenceType("ANNOUNCEMENT");
            request.setActionUrl("/announcements/" + announcementId);
            
            List<NotificationDto> notifications = createBulkNotifications(request, senderId);
            return CompletableFuture.completedFuture(notifications);
            
        } catch (Exception e) {
            log.error("Failed to send announcement notifications: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private void sendRealTimeNotification(NotificationDto notification) {
        try {
            // Send to user's personal notification queue
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId().toString(),
                    "/queue/notifications",
                    notification
            );
            
            // Also publish to Redis for other instances (if using multiple app instances)
            redisTemplate.convertAndSend("notifications:" + notification.getUserId(), notification);
            
        } catch (Exception e) {
            log.error("Failed to send real-time notification to user {}: {}", 
                     notification.getUserId(), e.getMessage());
        }
    }

    private void updateUnreadCountCache(Long userId) {
        try {
            String cacheKey = "notification:unread:" + userId;
            long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
            redisTemplate.opsForValue().set(cacheKey, count, java.time.Duration.ofMinutes(5));
            
            // Send real-time unread count update
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications/count",
                    java.util.Map.of("unreadCount", count)
            );
            
        } catch (Exception e) {
            log.error("Failed to update unread count cache for user {}: {}", userId, e.getMessage());
        }
    }

    private NotificationDto convertToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .priority(notification.getPriority())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .senderId(notification.getSenderId())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .actionUrl(notification.getActionUrl())
                .metadata(notification.getMetadata())
                .expiresAt(notification.getExpiresAt())
                .canMarkAsRead(!notification.getIsRead())
                .build();
    }
}