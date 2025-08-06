package com.example.demo.communication.email.dto;

import com.example.demo.communication.email.entity.TemplateType;
import com.example.demo.communication.email.entity.TemplateCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDto {
    
    private Long id;
    
    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name must not exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Template code is required")
    @Size(max = 50, message = "Template code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private TemplateType templateType;
    
    private TemplateCategory category;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private List<String> variables; // Available template variables
    
    private boolean isDefault;
    
    private boolean enabled;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    private Long createdBy;
    
    private String createdByName; // Creator name for display
    
    private Long updatedBy;
    
    private String updatedByName; // Updater name for display
}