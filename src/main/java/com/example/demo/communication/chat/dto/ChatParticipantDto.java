package com.example.demo.communication.chat.dto;

import com.example.demo.communication.chat.entity.ChatParticipantRole;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantDto {
    private Long id;
    private Long roomId;
    private Long userId;
    private ChatParticipantRole role;
    private Instant joinedAt;
    private Instant lastReadAt;
    private Long lastReadMessageId;
    private boolean isMuted;
    private boolean isActive;
    private Instant leftAt;
    
    // Additional fields for UI
    private String userName;
    private String userAvatar;
    private boolean isOnline;
}