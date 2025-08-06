package com.example.demo.communication.announcement.dto;

import com.example.demo.communication.announcement.entity.Announcement.AnnouncementPriority;
import com.example.demo.communication.announcement.entity.AnnouncementTarget;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private AnnouncementTarget targetAudience;
    private Long departmentId;
    private String roleName;
    private LocalDate publishDate;
    private LocalDate expiryDate;
    private boolean published;
    private AnnouncementPriority priority;
    private Instant createdAt;
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;
    
    // Additional fields for UI
    private String authorName;
    private String departmentName;
    private boolean isExpired;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canPublish;
    private int daysUntilExpiry;
}