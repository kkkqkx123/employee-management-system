package com.example.demo.communication.chat.controller;

import com.example.demo.communication.chat.dto.*;
import com.example.demo.communication.chat.service.ChatService;
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
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    @PreAuthorize("hasAuthority('CHAT_CREATE')")
    public ResponseEntity<ApiResponse<ChatRoomDto>> createChatRoom(
            @Valid @RequestBody CreateChatRoomRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        ChatRoomDto chatRoom = chatService.createChatRoom(
                request.getName(),
                request.getType(),
                request.getDescription(),
                userId,
                request.getParticipantIds()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(chatRoom, "Chat room created successfully"));
    }

    @GetMapping("/rooms/{roomId}")
    @PreAuthorize("hasAuthority('CHAT_READ')")
    public ResponseEntity<ApiResponse<ChatRoomDto>> getChatRoom(
            @PathVariable Long roomId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        ChatRoomDto chatRoom = chatService.getChatRoom(roomId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(chatRoom));
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasAuthority('CHAT_READ')")
    public ResponseEntity<ApiResponse<List<ChatRoomDto>>> getUserChatRooms(
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        List<ChatRoomDto> chatRooms = chatService.getUserChatRooms(userId);
        
        return ResponseEntity.ok(ApiResponse.success(chatRooms));
    }

    @PostMapping("/messages")
    @PreAuthorize("hasAuthority('CHAT_SEND')")
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        ChatMessageDto message = chatService.sendMessage(
                request.getRoomId(),
                userId,
                request.getContent(),
                request.getMessageType()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, "Message sent successfully"));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @PreAuthorize("hasAuthority('CHAT_READ')")
    public ResponseEntity<ApiResponse<Page<ChatMessageDto>>> getChatMessages(
            @PathVariable Long roomId,
            Pageable pageable,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Page<ChatMessageDto> messages = chatService.getChatMessages(roomId, userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PutMapping("/messages/{messageId}")
    @PreAuthorize("hasAuthority('CHAT_EDIT')")
    public ResponseEntity<ApiResponse<ChatMessageDto>> editMessage(
            @PathVariable Long messageId,
            @RequestBody String newContent,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        ChatMessageDto message = chatService.editMessage(messageId, userId, newContent);
        
        return ResponseEntity.ok(ApiResponse.success(message, "Message updated successfully"));
    }

    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("hasAuthority('CHAT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        chatService.deleteMessage(messageId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Message deleted successfully"));
    }

    @PostMapping("/rooms/{roomId}/participants")
    @PreAuthorize("hasAuthority('CHAT_MANAGE')")
    public ResponseEntity<ApiResponse<ChatParticipantDto>> addParticipant(
            @PathVariable Long roomId,
            @RequestParam Long userId,
            Authentication authentication) {
        
        Long addedBy = getCurrentUserId(authentication);
        ChatParticipantDto participant = chatService.addParticipant(roomId, userId, addedBy);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(participant, "Participant added successfully"));
    }

    @DeleteMapping("/rooms/{roomId}/participants/{userId}")
    @PreAuthorize("hasAuthority('CHAT_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> removeParticipant(
            @PathVariable Long roomId,
            @PathVariable Long userId,
            Authentication authentication) {
        
        Long removedBy = getCurrentUserId(authentication);
        chatService.removeParticipant(roomId, userId, removedBy);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Participant removed successfully"));
    }

    @PutMapping("/rooms/{roomId}/read")
    @PreAuthorize("hasAuthority('CHAT_READ')")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable Long roomId,
            @RequestParam Long lastReadMessageId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        chatService.markMessagesAsRead(roomId, userId, lastReadMessageId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as read"));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('CHAT_READ')")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Long unreadCount = chatService.getUnreadMessageCount(userId);
        
        return ResponseEntity.ok(ApiResponse.success(unreadCount));
    }

    @GetMapping("/rooms/{roomId}/unread-count")
    @PreAuthorize("hasAuthority('CHAT_READ')")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(
            @PathVariable Long roomId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Long unreadCount = chatService.getUnreadMessageCount(roomId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(unreadCount));
    }

    @GetMapping("/rooms/{roomId}/messages/search")
    @PreAuthorize("hasAuthority('CHAT_READ')")
    public ResponseEntity<ApiResponse<Page<ChatMessageDto>>> searchMessages(
            @PathVariable Long roomId,
            @RequestParam String searchTerm,
            Pageable pageable,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Page<ChatMessageDto> messages = chatService.searchMessages(roomId, userId, searchTerm, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    private Long getCurrentUserId(Authentication authentication) {
        // This should extract user ID from the authentication object
        // Implementation depends on your security setup
        return Long.parseLong(authentication.getName());
    }
}