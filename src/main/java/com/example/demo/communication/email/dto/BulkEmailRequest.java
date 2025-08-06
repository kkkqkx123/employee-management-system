package com.example.demo.communication.email.dto;

import com.example.demo.communication.email.entity.EmailPriority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmailRequest {
    
    @NotEmpty(message = "Recipient list cannot be empty")
    private List<@Email String> recipients;
    
    private List<@Email String> ccEmails;
    
    private List<@Email String> bccEmails;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String templateCode; // Optional: use template instead of direct content
    
    private Map<String, Object> templateVariables; // Variables for template processing
    
    @Builder.Default
    private EmailPriority priority = EmailPriority.NORMAL;
    
    private Long sentBy; // User ID of sender
    
    @Builder.Default
    private boolean isHtml = true; // Whether content is HTML
    
    @Builder.Default
    private int batchSize = 50; // Number of emails to send in each batch
    
    @Builder.Default
    private long delayBetweenBatches = 1000; // Delay in milliseconds between batches
}