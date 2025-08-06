package com.example.demo.communication.announcement.service.impl;

import com.example.demo.communication.announcement.dto.AnnouncementCreateRequest;
import com.example.demo.communication.announcement.dto.AnnouncementDto;
import com.example.demo.communication.announcement.dto.AnnouncementStatisticsDto;
import com.example.demo.communication.announcement.dto.AnnouncementUpdateRequest;
import com.example.demo.communication.announcement.entity.Announcement;
import com.example.demo.communication.announcement.entity.AnnouncementTarget;
import com.example.demo.communication.announcement.repository.AnnouncementRepository;
import com.example.demo.communication.announcement.service.AnnouncementService;
import com.example.demo.communication.notification.service.NotificationService;
import com.example.demo.communication.notification.entity.NotificationType;
import com.example.demo.communication.exception.AnnouncementNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public AnnouncementDto createAnnouncement(AnnouncementCreateRequest request, Long authorId) {
        log.info("Creating announcement: {} by user {}", request.getTitle(), authorId);
        
        validateAnnouncementRequest(request);
        
        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(authorId)
                .targetAudience(request.getTargetAudience())
                .departmentId(request.getDepartmentId())
                .roleName(request.getRoleName())
                .publishDate(request.getPublishDate())
                .expiryDate(request.getExpiryDate())
                .published(request.isPublished())
                .priority(request.getPriority())
                .createdBy(authorId)
                .build();
        
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        
        // If published immediately, trigger notifications
        if (savedAnnouncement.isPublished()) {
            sendAnnouncementNotifications(savedAnnouncement);
        }
        
        log.info("Announcement created successfully: {}", savedAnnouncement.getId());
        return convertToDto(savedAnnouncement);
    }

    @Override
    @Transactional
    public AnnouncementDto updateAnnouncement(Long id, AnnouncementUpdateRequest request, Long updatedBy) {
        log.info("Updating announcement {} by user {}", id, updatedBy);
        
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new AnnouncementNotFoundException("Announcement not found: " + id));
        
        validateAnnouncementRequest(request);
        
        boolean wasPublished = announcement.isPublished();
        
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setTargetAudience(request.getTargetAudience());
        announcement.setDepartmentId(request.getDepartmentId());
        announcement.setRoleName(request.getRoleName());
        announcement.setPublishDate(request.getPublishDate());
        announcement.setExpiryDate(request.getExpiryDate());
        announcement.setPublished(request.isPublished());
        announcement.setPriority(request.getPriority());
        announcement.setUpdatedBy(updatedBy);
        
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        
        // If newly published or content changed while published, send notifications
        if (savedAnnouncement.isPublished() && (!wasPublished || !wasPublished)) {
            sendAnnouncementNotifications(savedAnnouncement);
        }
        
        log.info("Announcement updated successfully: {}", savedAnnouncement.getId());
        return convertToDto(savedAnnouncement);
    }

    @Override
    public AnnouncementDto getAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new AnnouncementNotFoundException("Announcement not found: " + id));
        return convertToDto(announcement);
    }

    @Override
    public Page<AnnouncementDto> getAllAnnouncements(Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findAll(pageable);
        return announcements.map(this::convertToDto);
    }

    @Override
    public Page<AnnouncementDto> getPublishedAnnouncements(Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable);
        return announcements.map(this::convertToDto);
    }

    @Override
    public Page<AnnouncementDto> getActiveAnnouncements(Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findActiveAnnouncements(LocalDate.now(), pageable);
        return announcements.map(this::convertToDto);
    }

    @Override
    public Page<AnnouncementDto> getAnnouncementsForUser(Long userId, Long departmentId, String roleName, Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findAnnouncementsForUser(
                LocalDate.now(), departmentId, roleName, pageable);
        return announcements.map(this::convertToDto);
    }

    @Override
    public Page<AnnouncementDto> getAnnouncementsByAuthor(Long authorId, Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
        return announcements.map(this::convertToDto);
    }

    @Override
    public Page<AnnouncementDto> getAnnouncementsByTarget(AnnouncementTarget targetAudience, Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findByTargetAudienceOrderByCreatedAtDesc(targetAudience, pageable);
        return announcements.map(this::convertToDto);
    }

    @Override
    @Transactional
    public AnnouncementDto publishAnnouncement(Long id, Long publishedBy) {
        log.info("Publishing announcement {} by user {}", id, publishedBy);
        
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new AnnouncementNotFoundException("Announcement not found: " + id));
        
        if (announcement.isPublished()) {
            throw new IllegalStateException("Announcement is already published");
        }
        
        announcement.setPublished(true);
        announcement.setPublishDate(LocalDate.now());
        announcement.setUpdatedBy(publishedBy);
        
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        
        // Send notifications to target audience
        sendAnnouncementNotifications(savedAnnouncement);
        
        log.info("Announcement published successfully: {}", savedAnnouncement.getId());
        return convertToDto(savedAnnouncement);
    }

    @Override
    @Transactional
    public AnnouncementDto unpublishAnnouncement(Long id, Long unpublishedBy) {
        log.info("Unpublishing announcement {} by user {}", id, unpublishedBy);
        
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new AnnouncementNotFoundException("Announcement not found: " + id));
        
        if (!announcement.isPublished()) {
            throw new IllegalStateException("Announcement is not published");
        }
        
        announcement.setPublished(false);
        announcement.setUpdatedBy(unpublishedBy);
        
        Announcement savedAnnouncement = announcementRepository.save(announcement);
        
        log.info("Announcement unpublished successfully: {}", savedAnnouncement.getId());
        return convertToDto(savedAnnouncement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id, Long deletedBy) {
        log.info("Deleting announcement {} by user {}", id, deletedBy);
        
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new AnnouncementNotFoundException("Announcement not found: " + id));
        
        announcementRepository.delete(announcement);
        
        log.info("Announcement deleted successfully: {}", id);
    }

    @Override
    public AnnouncementStatisticsDto getAnnouncementStatistics() {
        long total = announcementRepository.count();
        long published = announcementRepository.countByPublishedTrue();
        List<Announcement> expired = announcementRepository.findExpiredAnnouncements(LocalDate.now());
        
        return AnnouncementStatisticsDto.builder()
                .totalAnnouncements(total)
                .publishedAnnouncements(published)
                .expiredAnnouncements(expired.size())
                .draftAnnouncements(total - published)
                .build();
    }

    @Override
    @Transactional
    public int cleanupExpiredAnnouncements() {
        List<Announcement> expiredAnnouncements = announcementRepository.findExpiredAnnouncements(LocalDate.now());
        
        // You might want to archive instead of delete
        // For now, we'll just log them
        log.info("Found {} expired announcements", expiredAnnouncements.size());
        
        return expiredAnnouncements.size();
    }

    private void sendAnnouncementNotifications(Announcement announcement) {
        try {
            String title = "New Announcement: " + announcement.getTitle();
            String content = announcement.getContent();
            
            // This will be handled by the database trigger we created in the migration
            // The trigger automatically creates notifications based on target audience
            log.info("Announcement notifications will be created by database trigger for announcement: {}", 
                    announcement.getId());
            
        } catch (Exception e) {
            log.error("Failed to send announcement notifications for announcement {}: {}", 
                     announcement.getId(), e.getMessage(), e);
        }
    }

    private void validateAnnouncementRequest(Object request) {
        AnnouncementTarget targetAudience;
        Long departmentId;
        String roleName;
        
        if (request instanceof AnnouncementCreateRequest) {
            AnnouncementCreateRequest createRequest = (AnnouncementCreateRequest) request;
            targetAudience = createRequest.getTargetAudience();
            departmentId = createRequest.getDepartmentId();
            roleName = createRequest.getRoleName();
        } else if (request instanceof AnnouncementUpdateRequest) {
            AnnouncementUpdateRequest updateRequest = (AnnouncementUpdateRequest) request;
            targetAudience = updateRequest.getTargetAudience();
            departmentId = updateRequest.getDepartmentId();
            roleName = updateRequest.getRoleName();
        } else {
            return;
        }
        
        if (targetAudience == AnnouncementTarget.DEPARTMENT && departmentId == null) {
            throw new IllegalArgumentException("Department ID is required when target audience is DEPARTMENT");
        }
        
        if (targetAudience == AnnouncementTarget.ROLE && (roleName == null || roleName.trim().isEmpty())) {
            throw new IllegalArgumentException("Role name is required when target audience is ROLE");
        }
    }

    private AnnouncementDto convertToDto(Announcement announcement) {
        AnnouncementDto dto = AnnouncementDto.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .authorId(announcement.getAuthorId())
                .targetAudience(announcement.getTargetAudience())
                .departmentId(announcement.getDepartmentId())
                .roleName(announcement.getRoleName())
                .publishDate(announcement.getPublishDate())
                .expiryDate(announcement.getExpiryDate())
                .published(announcement.isPublished())
                .priority(announcement.getPriority())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .createdBy(announcement.getCreatedBy())
                .updatedBy(announcement.getUpdatedBy())
                .build();
        
        // Set additional UI fields
        if (announcement.getExpiryDate() != null) {
            dto.setExpired(announcement.getExpiryDate().isBefore(LocalDate.now()));
            dto.setDaysUntilExpiry((int) ChronoUnit.DAYS.between(LocalDate.now(), announcement.getExpiryDate()));
        }
        
        return dto;
    }
}