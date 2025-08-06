package com.example.demo.communication.chat.repository;

import com.example.demo.communication.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Find messages in a chat room with pagination
     */
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.room.id = :roomId AND cm.isDeleted = false " +
           "ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") Long roomId, Pageable pageable);
    
    /**
     * Find messages after a specific timestamp
     */
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.room.id = :roomId AND cm.createdAt > :timestamp AND cm.isDeleted = false " +
           "ORDER BY cm.createdAt ASC")
    List<ChatMessage> findMessagesAfter(@Param("roomId") Long roomId, @Param("timestamp") Instant timestamp);
    
    /**
     * Search messages in a room
     */
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.room.id = :roomId AND cm.isDeleted = false " +
           "AND LOWER(cm.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY cm.createdAt DESC")
    Page<ChatMessage> searchMessagesInRoom(@Param("roomId") Long roomId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Count unread messages for user in room
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
           "WHERE cm.room.id = :roomId AND cm.senderId != :userId " +
           "AND cm.createdAt > (SELECT COALESCE(cp.lastReadAt, '1970-01-01T00:00:00Z') " +
           "                   FROM ChatParticipant cp " +
           "                   WHERE cp.room.id = :roomId AND cp.userId = :userId) " +
           "AND cm.isDeleted = false")
    Long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);
    
    /**
     * Find latest message in room
     */
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.room.id = :roomId AND cm.isDeleted = false " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLatestMessage(@Param("roomId") Long roomId, Pageable pageable);
    
    /**
     * Count total unread messages for user across all rooms
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
           "JOIN ChatParticipant cp ON cm.room.id = cp.room.id " +
           "WHERE cp.userId = :userId AND cm.senderId != :userId " +
           "AND cm.createdAt > COALESCE(cp.lastReadAt, '1970-01-01T00:00:00Z') " +
           "AND cm.isDeleted = false AND cp.isActive = true")
    Long countTotalUnreadMessages(@Param("userId") Long userId);
}