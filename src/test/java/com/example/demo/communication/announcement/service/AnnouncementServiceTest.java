package com.example.demo.communication.announcement.service;

import com.example.demo.communication.announcement.dto.AnnouncementCreateRequest;
import com.example.demo.communication.announcement.dto.AnnouncementDto;
import com.example.demo.communication.announcement.entity.Announcement;
import com.example.demo.communication.announcement.entity.AnnouncementTarget;
import com.example.demo.communication.announcement.repository.AnnouncementRepository;
import com.example.demo.communication.announcement.service.impl.AnnouncementServiceImpl;
import com.example.demo.communication.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AnnouncementServiceImpl announcementService;

    private AnnouncementCreateRequest createRequest;
    private Announcement announcement;

    @BeforeEach
    void setUp() {
        createRequest = new AnnouncementCreateRequest();
        createRequest.setTitle("Test Announcement");
        createRequest.setContent("This is a test announcement");
        createRequest.setTargetAudience(AnnouncementTarget.ALL);
        createRequest.setPublished(false);
        createRequest.setPriority(Announcement.AnnouncementPriority.NORMAL);

        announcement = Announcement.builder()
                .id(1L)
                .title("Test Announcement")
                .content("This is a test announcement")
                .authorId(1L)
                .targetAudience(AnnouncementTarget.ALL)
                .published(false)
                .priority(Announcement.AnnouncementPriority.NORMAL)
                .build();
    }

    @Test
    void createAnnouncement_ShouldCreateSuccessfully() {
        // Given
        when(announcementRepository.save(any(Announcement.class))).thenReturn(announcement);

        // When
        AnnouncementDto result = announcementService.createAnnouncement(createRequest, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Test Announcement", result.getTitle());
        assertEquals("This is a test announcement", result.getContent());
        assertEquals(AnnouncementTarget.ALL, result.getTargetAudience());
        assertFalse(result.isPublished());

        verify(announcementRepository).save(any(Announcement.class));
    }

    @Test
    void createAnnouncement_WithDepartmentTarget_ShouldRequireDepartmentId() {
        // Given
        createRequest.setTargetAudience(AnnouncementTarget.DEPARTMENT);
        createRequest.setDepartmentId(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> announcementService.createAnnouncement(createRequest, 1L));
    }

    @Test
    void createAnnouncement_WithRoleTarget_ShouldRequireRoleName() {
        // Given
        createRequest.setTargetAudience(AnnouncementTarget.ROLE);
        createRequest.setRoleName(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> announcementService.createAnnouncement(createRequest, 1L));
    }

    @Test
    void getAnnouncement_ShouldReturnAnnouncement() {
        // Given
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(announcement));

        // When
        AnnouncementDto result = announcementService.getAnnouncement(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Announcement", result.getTitle());
    }

    @Test
    void publishAnnouncement_ShouldPublishSuccessfully() {
        // Given
        announcement.setPublished(false);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(announcement));
        when(announcementRepository.save(any(Announcement.class))).thenReturn(announcement);

        // When
        AnnouncementDto result = announcementService.publishAnnouncement(1L, 1L);

        // Then
        assertTrue(result.isPublished());
        verify(announcementRepository).save(any(Announcement.class));
    }

    @Test
    void publishAnnouncement_AlreadyPublished_ShouldThrowException() {
        // Given
        announcement.setPublished(true);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(announcement));

        // When & Then
        assertThrows(IllegalStateException.class, 
                () -> announcementService.publishAnnouncement(1L, 1L));
    }
}