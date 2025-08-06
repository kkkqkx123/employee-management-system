package com.example.demo.communication.announcement.dto;

import com.example.demo.communication.announcement.entity.Announcement.AnnouncementPriority;
import com.example.demo.communication.announcement.entity.AnnouncementTarget;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AnnouncementCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Target audience is required")
    private AnnouncementTarget targetAudience;
    
    private Long departmentId; // Required if targetAudience is DEPARTMENT
    
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    private String roleName; // Required if targetAudience is ROLE
    
    private LocalDate publishDate;
    
    private LocalDate expiryDate;
    
    private boolean published = false;
    
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;
}