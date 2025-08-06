package com.example.demo.communication.chat.service;

import com.example.demo.communication.chat.dto.ChatMessageDto;
import com.example.demo.communication.chat.dto.ChatRoomDto;
import com.example.demo.communication.chat.dto.ChatParticipantDto;
import com.example.demo.communication.chat.entity.ChatRoomType;
import com.example.demo.communication.chat.entity.ChatMessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    
    /**
     * Create a new chat room
     * @param name Room name
     * @param type Room type (DIRECT, GROUP, CHANNEL)
     * @param description Room description
     * @param createdBy Creator user ID
     * @param participantIds Initial participant user IDs
     * @return Created chat room DTO
     */
    ChatRoomDto createChatRoom(String name, ChatRoomType type, String description, Long createdBy, List<Long> participantIds);
    
    /**
     * Get chat room by ID
     * @param roomId Room ID
     * @param userId Current user ID (for permission check)
     * @return Chat room DTO
     */
    ChatRoomDto getChatRoom(Long roomId, Long userId);
    
    /**
     * Get user's chat rooms
     * @param userId User ID
     * @return List of chat room DTOs
     */
    List<ChatRoomDto> getUserChatRooms(Long userId);
    
    /**
     * Send message to chat room
     * @param roomId Room ID
     * @param senderId Sender user ID
     * @param content Message content
     * @param messageType Message type
     * @return Created message DTO
     */
    ChatMessageDto sendMessage(Long roomId, Long senderId, String content, ChatMessageType messageType);
    
    /**
     * Get chat messages for room
     * @param roomId Room ID
     * @param userId Current user ID (for permission check)
     * @param pageable Pagination parameters
     * @return Page of message DTOs
     */
    Page<ChatMessageDto> getChatMessages(Long roomId, Long userId, Pageable pageable);
    
    /**
     * Edit message
     * @param messageId Message ID
     * @param userId User ID (must be message sender)
     * @param newContent New message content
     * @return Updated message DTO
     */
    ChatMessageDto editMessage(Long messageId, Long userId, String newContent);
    
    /**
     * Delete message
     * @param messageId Message ID
     * @param userId User ID (must be message sender or room admin)
     */
    void deleteMessage(Long messageId, Long userId);
    
    /**
     * Add participant to chat room
     * @param roomId Room ID
     * @param userId User ID to add
     * @param addedBy User ID who is adding (must have permission)
     * @return Chat participant DTO
     */
    ChatParticipantDto addParticipant(Long roomId, Long userId, Long addedBy);
    
    /**
     * Remove participant from chat room
     * @param roomId Room ID
     * @param userId User ID to remove
     * @param removedBy User ID who is removing (must have permission)
     */
    void removeParticipant(Long roomId, Long userId, Long removedBy);
    
    /**
     * Mark messages as read
     * @param roomId Room ID
     * @param userId User ID
     * @param lastReadMessageId Last message ID read
     */
    void markMessagesAsRead(Long roomId, Long userId, Long lastReadMessageId);
    
    /**
     * Get unread message count for user
     * @param userId User ID
     * @return Total unread message count across all rooms
     */
    Long getUnreadMessageCount(Long userId);
    
    /**
     * Get unread message count for specific room
     * @param roomId Room ID
     * @param userId User ID
     * @return Unread message count for the room
     */
    Long getUnreadMessageCount(Long roomId, Long userId);
    
    /**
     * Search messages in room
     * @param roomId Room ID
     * @param userId Current user ID (for permission check)
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Page of matching message DTOs
     */
    Page<ChatMessageDto> searchMessages(Long roomId, Long userId, String searchTerm, Pageable pageable);
}