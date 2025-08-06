package com.example.demo.communication.chat.repository;

import com.example.demo.communication.chat.entity.ChatRoom;
import com.example.demo.communication.chat.entity.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    /**
     * Find chat rooms by type
     */
    List<ChatRoom> findByType(ChatRoomType type);
    
    /**
     * Find active chat rooms by creator
     */
    List<ChatRoom> findByCreatedByAndIsActiveTrue(Long createdBy);
    
    /**
     * Find chat rooms where user is a participant
     */
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
           "JOIN cr.participants cp " +
           "WHERE cp.userId = :userId AND cp.isActive = true AND cr.isActive = true " +
           "ORDER BY cr.lastMessageAt DESC NULLS LAST")
    List<ChatRoom> findUserChatRooms(@Param("userId") Long userId);
    
    /**
     * Find direct chat room between two users
     */
    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.type = 'DIRECT' AND cr.isActive = true " +
           "AND EXISTS (SELECT 1 FROM ChatParticipant cp1 WHERE cp1.room = cr AND cp1.userId = :userId1 AND cp1.isActive = true) " +
           "AND EXISTS (SELECT 1 FROM ChatParticipant cp2 WHERE cp2.room = cr AND cp2.userId = :userId2 AND cp2.isActive = true)")
    Optional<ChatRoom> findDirectChatRoom(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * Check if user has access to chat room
     */
    @Query("SELECT COUNT(cr) > 0 FROM ChatRoom cr " +
           "JOIN cr.participants cp " +
           "WHERE cr.id = :roomId AND cp.userId = :userId AND cp.isActive = true")
    boolean hasUserAccess(@Param("roomId") Long roomId, @Param("userId") Long userId);
}