package com.example.demo.communication.chat.dto;

import com.example.demo.communication.chat.entity.ChatRoomType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private Long id;
    private String name;
    private ChatRoomType type;
    private String description;
    private String avatarUrl;
    private Long createdBy;
    private boolean isPrivate;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastMessageAt;
    private Long lastMessageId;
    
    // Additional fields for UI
    private Long unreadCount;
    private String lastMessageContent;
    private String lastMessageSender;
    private List<ChatParticipantDto> participants;
    private int participantCount;
}