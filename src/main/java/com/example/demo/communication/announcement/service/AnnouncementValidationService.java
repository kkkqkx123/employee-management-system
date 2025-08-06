package com.example.demo.communication.announcement.service;

import com.example.demo.communication.announcement.dto.AnnouncementCreateRequest;
import com.example.demo.communication.announcement.dto.AnnouncementUpdateRequest;
import com.example.demo.communication.announcement.entity.AnnouncementTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementValidationService {

    /**
     * Validate announcement creation request
     */
    public void validateCreateRequest(AnnouncementCreateRequest request) {
        validateCommonFields(request.getTargetAudience(), request.getDepartmentId(), 
                           request.getRoleName(), request.getPublishDate(), request.getExpiryDate());
    }

    /**
     * Validate announcement update request
     */
    public void validateUpdateRequest(AnnouncementUpdateRequest request) {
        validateCommonFields(request.getTargetAudience(), request.getDepartmentId(), 
                           request.getRoleName(), request.getPublishDate(), request.getExpiryDate());
    }

    /**
     * Validate common fields across create and update requests
     */
    private void validateCommonFields(AnnouncementTarget targetAudience, Long departmentId, 
                                    String roleName, LocalDate publishDate, LocalDate expiryDate) {
        
        // Validate target audience specific requirements
        if (targetAudience == AnnouncementTarget.DEPARTMENT && departmentId == null) {
            throw new IllegalArgumentException("Department ID is required when target audience is DEPARTMENT");
        }
        
        if (targetAudience == AnnouncementTarget.ROLE && (roleName == null || roleName.trim().isEmpty())) {
            throw new IllegalArgumentException("Role name is required when target audience is ROLE");
        }
        
        // Validate date logic
        if (publishDate != null && publishDate.isBefore(LocalDate.now())) {
            log.warn("Publish date is in the past: {}", publishDate);
        }
        
        if (expiryDate != null) {
            if (expiryDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Expiry date cannot be in the past");
            }
            
            if (publishDate != null && expiryDate.isBefore(publishDate)) {
                throw new IllegalArgumentException("Expiry date cannot be before publish date");
            }
        }
    }

    /**
     * Check if user can publish announcement
     */
    public boolean canUserPublishAnnouncement(Long userId, AnnouncementTarget targetAudience, Long departmentId) {
        // Add your business logic here
        // For example, check if user has permission to publish to specific departments
        return true; // Simplified for now
    }

    /**
     * Check if user can edit announcement
     */
    public boolean canUserEditAnnouncement(Long userId, Long authorId) {
        // Users can edit their own announcements, or admins can edit any
        return userId.equals(authorId); // Simplified for now
    }

    /**
     * Check if user can delete announcement
     */
    public boolean canUserDeleteAnnouncement(Long userId, Long authorId) {
        // Users can delete their own announcements, or admins can delete any
        return userId.equals(authorId); // Simplified for now
    }
}