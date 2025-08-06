package com.example.demo.communication.chat.dto;

import com.example.demo.communication.chat.entity.ChatMessageType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String content;
    private ChatMessageType messageType;
    private Instant createdAt;
    private Boolean isEdited;
    private Instant editedAt;
    private Boolean isDeleted;
    private Instant deletedAt;
    
    // Additional fields for UI
    private String senderName;
    private String senderAvatar;
    private boolean isRead;
    private boolean canEdit;
    private boolean canDelete;
}