package com.example.demo.communication.chat.dto;

import com.example.demo.communication.chat.entity.ChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotBlank(message = "Message content is required")
    private String content;
    
    private ChatMessageType messageType = ChatMessageType.TEXT;
}