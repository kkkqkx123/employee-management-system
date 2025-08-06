package com.example.demo.communication.chat.websocket;

import com.example.demo.communication.chat.dto.ChatMessageDto;
import com.example.demo.communication.chat.dto.SendMessageRequest;
import com.example.demo.communication.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages
     */
    @MessageMapping("/chat/room/{roomId}/send")
    @SendTo("/topic/chat/room/{roomId}")
    public ChatMessageDto sendMessage(
            @DestinationVariable Long roomId,
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            Long userId = getUserIdFromPrincipal(principal);
            log.info("Received message from user {} for room {}: {}", userId, roomId, request.getContent());
            
            ChatMessageDto message = chatService.sendMessage(
                    roomId,
                    userId,
                    request.getContent(),
                    request.getMessageType()
            );
            
            log.info("Message sent successfully: {}", message.getId());
            return message;
            
        } catch (Exception e) {
            log.error("Error sending message to room {}: {}", roomId, e.getMessage(), e);
            // Send error message back to sender
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    "Failed to send message: " + e.getMessage()
            );
            return null;
        }
    }

    /**
     * Handle typing indicators
     */
    @MessageMapping("/chat/room/{roomId}/typing")
    public void handleTyping(
            @DestinationVariable Long roomId,
            @Payload TypingIndicator typingIndicator,
            Principal principal) {
        
        try {
            Long userId = getUserIdFromPrincipal(principal);
            typingIndicator.setUserId(userId);
            
            // Broadcast typing indicator to all room participants except sender
            messagingTemplate.convertAndSend(
                    "/topic/chat/room/" + roomId + "/typing",
                    typingIndicator
            );
            
        } catch (Exception e) {
            log.error("Error handling typing indicator for room {}: {}", roomId, e.getMessage());
        }
    }

    /**
     * Handle user joining a room
     */
    @MessageMapping("/chat/room/{roomId}/join")
    public void handleJoinRoom(
            @DestinationVariable Long roomId,
            Principal principal) {
        
        try {
            Long userId = getUserIdFromPrincipal(principal);
            log.info("User {} joining room {}", userId, roomId);
            
            // Notify other participants that user joined
            UserPresence presence = new UserPresence(userId, true);
            messagingTemplate.convertAndSend(
                    "/topic/chat/room/" + roomId + "/presence",
                    presence
            );
            
        } catch (Exception e) {
            log.error("Error handling room join for room {}: {}", roomId, e.getMessage());
        }
    }

    /**
     * Handle user leaving a room
     */
    @MessageMapping("/chat/room/{roomId}/leave")
    public void handleLeaveRoom(
            @DestinationVariable Long roomId,
            Principal principal) {
        
        try {
            Long userId = getUserIdFromPrincipal(principal);
            log.info("User {} leaving room {}", userId, roomId);
            
            // Notify other participants that user left
            UserPresence presence = new UserPresence(userId, false);
            messagingTemplate.convertAndSend(
                    "/topic/chat/room/" + roomId + "/presence",
                    presence
            );
            
        } catch (Exception e) {
            log.error("Error handling room leave for room {}: {}", roomId, e.getMessage());
        }
    }

    /**
     * Handle message read receipts
     */
    @MessageMapping("/chat/room/{roomId}/read")
    public void handleMessageRead(
            @DestinationVariable Long roomId,
            @Payload ReadReceipt readReceipt,
            Principal principal) {
        
        try {
            Long userId = getUserIdFromPrincipal(principal);
            
            // Update read status in database
            chatService.markMessagesAsRead(roomId, userId, readReceipt.getLastReadMessageId());
            
            // Notify other participants about read receipt
            readReceipt.setUserId(userId);
            messagingTemplate.convertAndSend(
                    "/topic/chat/room/" + roomId + "/read",
                    readReceipt
            );
            
        } catch (Exception e) {
            log.error("Error handling read receipt for room {}: {}", roomId, e.getMessage());
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
    public static class TypingIndicator {
        private Long userId;
        private boolean isTyping;
        
        // Constructors, getters, setters
        public TypingIndicator() {}
        
        public TypingIndicator(Long userId, boolean isTyping) {
            this.userId = userId;
            this.isTyping = isTyping;
        }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public boolean isTyping() { return isTyping; }
        public void setTyping(boolean typing) { isTyping = typing; }
    }

    public static class UserPresence {
        private Long userId;
        private boolean isOnline;
        
        public UserPresence() {}
        
        public UserPresence(Long userId, boolean isOnline) {
            this.userId = userId;
            this.isOnline = isOnline;
        }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public boolean isOnline() { return isOnline; }
        public void setOnline(boolean online) { isOnline = online; }
    }

    public static class ReadReceipt {
        private Long userId;
        private Long lastReadMessageId;
        
        public ReadReceipt() {}
        
        public ReadReceipt(Long userId, Long lastReadMessageId) {
            this.userId = userId;
            this.lastReadMessageId = lastReadMessageId;
        }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getLastReadMessageId() { return lastReadMessageId; }
        public void setLastReadMessageId(Long lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }
    }
}