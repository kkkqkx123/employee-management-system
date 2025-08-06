package com.example.demo.communication.announcement.controller;

import com.example.demo.communication.announcement.dto.AnnouncementCreateRequest;
import com.example.demo.communication.announcement.dto.AnnouncementDto;
import com.example.demo.communication.announcement.dto.AnnouncementStatisticsDto;
import com.example.demo.communication.announcement.dto.AnnouncementUpdateRequest;
import com.example.demo.communication.announcement.entity.AnnouncementTarget;
import com.example.demo.communication.announcement.service.AnnouncementService;
import com.example.demo.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_CREATE')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> createAnnouncement(
            @Valid @RequestBody AnnouncementCreateRequest request,
            Authentication authentication) {
        
        Long authorId = getCurrentUserId(authentication);
        AnnouncementDto announcement = announcementService.createAnnouncement(request, authorId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(announcement, "Announcement created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_UPDATE')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementUpdateRequest request,
            Authentication authentication) {
        
        Long updatedBy = getCurrentUserId(authentication);
        AnnouncementDto announcement = announcementService.updateAnnouncement(id, request, updatedBy);
        
        return ResponseEntity.ok(ApiResponse.success(announcement, "Announcement updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_READ')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> getAnnouncement(@PathVariable Long id) {
        AnnouncementDto announcement = announcementService.getAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success(announcement));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_READ')")
    public ResponseEntity<ApiResponse<Page<AnnouncementDto>>> getAllAnnouncements(Pageable pageable) {
        Page<AnnouncementDto> announcements = announcementService.getAllAnnouncements(pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/published")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_READ')")
    public ResponseEntity<ApiResponse<Page<AnnouncementDto>>> getPublishedAnnouncements(Pageable pageable) {
        Page<AnnouncementDto> announcements = announcementService.getPublishedAnnouncements(pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_READ')")
    public ResponseEntity<ApiResponse<Page<AnnouncementDto>>> getActiveAnnouncements(Pageable pageable) {
        Page<AnnouncementDto> announcements = announcementService.getActiveAnnouncements(pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/my-announcements")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_READ')")
    public ResponseEntity<ApiResponse<Page<AnnouncementDto>>> getAnnouncementsForUser(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String roleName,
            Pageable pageable,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        Page<AnnouncementDto> announcements = announcementService.getAnnouncementsForUser(
                userId, departmentId, roleName, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/by-author/{authorId}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_READ')")
    public ResponseEntity<ApiResponse<Page<AnnouncementDto>>> getAnnouncementsByAuthor(
            @PathVariable Long authorId,
            Pageable pageable) {
        
        Page<AnnouncementDto> announcements = announcementService.getAnnouncementsByAuthor(authorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/by-target/{targetAudience}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_READ')")
    public ResponseEntity<ApiResponse<Page<AnnouncementDto>>> getAnnouncementsByTarget(
            @PathVariable AnnouncementTarget targetAudience,
            Pageable pageable) {
        
        Page<AnnouncementDto> announcements = announcementService.getAnnouncementsByTarget(targetAudience, pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_PUBLISH')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> publishAnnouncement(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long publishedBy = getCurrentUserId(authentication);
        AnnouncementDto announcement = announcementService.publishAnnouncement(id, publishedBy);
        
        return ResponseEntity.ok(ApiResponse.success(announcement, "Announcement published successfully"));
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_PUBLISH')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> unpublishAnnouncement(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long unpublishedBy = getCurrentUserId(authentication);
        AnnouncementDto announcement = announcementService.unpublishAnnouncement(id, unpublishedBy);
        
        return ResponseEntity.ok(ApiResponse.success(announcement, "Announcement unpublished successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long deletedBy = getCurrentUserId(authentication);
        announcementService.deleteAnnouncement(id, deletedBy);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Announcement deleted successfully"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_READ')")
    public ResponseEntity<ApiResponse<AnnouncementStatisticsDto>> getAnnouncementStatistics() {
        AnnouncementStatisticsDto statistics = announcementService.getAnnouncementStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_MANAGE')")
    public ResponseEntity<ApiResponse<Integer>> cleanupExpiredAnnouncements() {
        int cleanedUp = announcementService.cleanupExpiredAnnouncements();
        return ResponseEntity.ok(ApiResponse.success(cleanedUp, 
                "Expired announcements cleanup completed"));
    }

    private Long getCurrentUserId(Authentication authentication) {
        // This should extract user ID from the authentication object
        // Implementation depends on your security setup
        return Long.parseLong(authentication.getName());
    }
}