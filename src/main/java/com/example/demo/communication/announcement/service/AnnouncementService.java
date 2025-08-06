package com.example.demo.communication.announcement.service;

import com.example.demo.communication.announcement.dto.AnnouncementCreateRequest;
import com.example.demo.communication.announcement.dto.AnnouncementDto;
import com.example.demo.communication.announcement.dto.AnnouncementStatisticsDto;
import com.example.demo.communication.announcement.dto.AnnouncementUpdateRequest;
import com.example.demo.communication.announcement.entity.AnnouncementTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnnouncementService {
    
    /**
     * Create a new announcement
     * @param request Announcement creation request
     * @param authorId User ID who is creating the announcement
     * @return Created announcement DTO
     */
    AnnouncementDto createAnnouncement(AnnouncementCreateRequest request, Long authorId);
    
    /**
     * Update an existing announcement
     * @param id Announcement ID
     * @param request Announcement update request
     * @param updatedBy User ID who is updating the announcement
     * @return Updated announcement DTO
     */
    AnnouncementDto updateAnnouncement(Long id, AnnouncementUpdateRequest request, Long updatedBy);
    
    /**
     * Get announcement by ID
     * @param id Announcement ID
     * @return Announcement DTO
     */
    AnnouncementDto getAnnouncement(Long id);
    
    /**
     * Get all announcements with pagination
     * @param pageable Pagination parameters
     * @return Page of announcement DTOs
     */
    Page<AnnouncementDto> getAllAnnouncements(Pageable pageable);
    
    /**
     * Get published announcements
     * @param pageable Pagination parameters
     * @return Page of published announcement DTOs
     */
    Page<AnnouncementDto> getPublishedAnnouncements(Pageable pageable);
    
    /**
     * Get active announcements (published and not expired)
     * @param pageable Pagination parameters
     * @return Page of active announcement DTOs
     */
    Page<AnnouncementDto> getActiveAnnouncements(Pageable pageable);
    
    /**
     * Get announcements for a specific user based on their department and role
     * @param userId User ID
     * @param departmentId User's department ID
     * @param roleName User's role name
     * @param pageable Pagination parameters
     * @return Page of announcement DTOs visible to the user
     */
    Page<AnnouncementDto> getAnnouncementsForUser(Long userId, Long departmentId, String roleName, Pageable pageable);
    
    /**
     * Get announcements by author
     * @param authorId Author user ID
     * @param pageable Pagination parameters
     * @return Page of announcement DTOs
     */
    Page<AnnouncementDto> getAnnouncementsByAuthor(Long authorId, Pageable pageable);
    
    /**
     * Get announcements by target audience
     * @param targetAudience Target audience type
     * @param pageable Pagination parameters
     * @return Page of announcement DTOs
     */
    Page<AnnouncementDto> getAnnouncementsByTarget(AnnouncementTarget targetAudience, Pageable pageable);
    
    /**
     * Publish an announcement
     * @param id Announcement ID
     * @param publishedBy User ID who is publishing
     * @return Updated announcement DTO
     */
    AnnouncementDto publishAnnouncement(Long id, Long publishedBy);
    
    /**
     * Unpublish an announcement
     * @param id Announcement ID
     * @param unpublishedBy User ID who is unpublishing
     * @return Updated announcement DTO
     */
    AnnouncementDto unpublishAnnouncement(Long id, Long unpublishedBy);
    
    /**
     * Delete an announcement
     * @param id Announcement ID
     * @param deletedBy User ID who is deleting
     */
    void deleteAnnouncement(Long id, Long deletedBy);
    
    /**
     * Get announcement statistics
     * @return Announcement statistics
     */
    AnnouncementStatisticsDto getAnnouncementStatistics();
    
    /**
     * Clean up expired announcements
     * @return Number of expired announcements cleaned up
     */
    int cleanupExpiredAnnouncements();
}