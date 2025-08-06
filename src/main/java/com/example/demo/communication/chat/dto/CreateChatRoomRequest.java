package com.example.demo.communication.chat.dto;

import com.example.demo.communication.chat.entity.ChatRoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateChatRoomRequest {
    @NotBlank(message = "Room name is required")
    @Size(max = 100, message = "Room name must not exceed 100 characters")
    private String name;
    
    @NotNull(message = "Room type is required")
    private ChatRoomType type;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private String avatarUrl;
    
    private boolean isPrivate = false;
    
    private List<Long> participantIds;
}