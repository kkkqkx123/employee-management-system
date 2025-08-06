package com.example.demo.communication.chat.repository;

import com.example.demo.communication.chat.entity.ChatParticipant;
import com.example.demo.communication.chat.entity.ChatParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    
    /**
     * Find participant by room and user
     */
    Optional<ChatParticipant> findByRoomIdAndUserId(Long roomId, Long userId);
    
    /**
     * Find active participants in a room
     */
    List<ChatParticipant> findByRoomIdAndIsActiveTrue(Long roomId);
    
    /**
     * Find participants by user ID
     */
    List<ChatParticipant> findByUserIdAndIsActiveTrue(Long userId);
    
    /**
     * Check if user is participant in room
     */
    boolean existsByRoomIdAndUserIdAndIsActiveTrue(Long roomId, Long userId);
    
    /**
     * Find participants with specific role in room
     */
    List<ChatParticipant> findByRoomIdAndRoleAndIsActiveTrue(Long roomId, ChatParticipantRole role);
    
    /**
     * Update last read timestamp for participant
     */
    @Modifying
    @Query("UPDATE ChatParticipant cp SET cp.lastReadAt = :timestamp, cp.lastReadMessageId = :messageId " +
           "WHERE cp.room.id = :roomId AND cp.userId = :userId")
    void updateLastRead(@Param("roomId") Long roomId, @Param("userId") Long userId, 
                       @Param("timestamp") Instant timestamp, @Param("messageId") Long messageId);
    
    /**
     * Remove participant from room (soft delete)
     */
    @Modifying
    @Query("UPDATE ChatParticipant cp SET cp.isActive = false, cp.leftAt = :leftAt " +
           "WHERE cp.room.id = :roomId AND cp.userId = :userId")
    void removeParticipant(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("leftAt") Instant leftAt);
    
    /**
     * Count active participants in room
     */
    long countByRoomIdAndIsActiveTrue(Long roomId);
}