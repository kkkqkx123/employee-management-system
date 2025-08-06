package com.example.demo.communication.notification.repository;

import com.example.demo.communication.notification.entity.Notification;
import com.example.demo.communication.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find notifications for a user with pagination
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find unread notifications for a user
     */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find notifications by type for a user
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type, Pageable pageable);
    
    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndIsReadFalse(Long userId);
    
    /**
     * Count unread notifications by type for a user
     */
    long countByUserIdAndTypeAndIsReadFalse(Long userId, NotificationType type);
    
    /**
     * Find notifications by reference
     */
    List<Notification> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    
    /**
     * Mark notification as read
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId, @Param("readAt") Instant readAt);
    
    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") Instant readAt);
    
    /**
     * Delete expired notifications
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    int deleteExpiredNotifications(@Param("now") Instant now);
    
    /**
     * Find notifications sent by a user
     */
    Page<Notification> findBySenderIdOrderByCreatedAtDesc(Long senderId, Pageable pageable);
    
    /**
     * Find recent notifications for a user (last 24 hours)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt > :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("userId") Long userId, @Param("since") Instant since);
}