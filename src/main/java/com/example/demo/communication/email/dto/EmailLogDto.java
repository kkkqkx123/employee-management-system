package com.example.demo.communication.email.dto;

import com.example.demo.communication.email.entity.EmailStatus;
import com.example.demo.communication.email.entity.EmailPriority;
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
public class EmailLogDto {
    
    private Long id;
    
    private String toEmail;
    
    private List<String> ccEmails;
    
    private List<String> bccEmails;
    
    private String subject;
    
    private String content;
    
    private String templateCode;
    
    private EmailStatus status;
    
    private String errorMessage;
    
    private Integer retryCount;
    
    private Instant sentAt;
    
    private Instant createdAt;
    
    private Long sentBy;
    
    private String sentByName; // Sender name for display
    
    private String messageId;
    
    private EmailPriority priority;
}