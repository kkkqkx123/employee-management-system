package com.example.demo.communication.announcement.service.impl;

import com.example.demo.communication.announcement.dto.AnnouncementCreateRequest;
import com.example.demo.communication.announcement.dto.AnnouncementDto;
import com.example.demo.communication.announcement.dto.AnnouncementUpdateRequest;
import com.example.demo.communication.announcement.entity.Announcement;
import com.example.demo.communication.announcement.repository.AnnouncementRepository;
import com.example.demo.communication.announcement.service.AnnouncementValidationService;
import com.example.demo.communication.notification.service.NotificationService;
import com.example.demo.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnnouncementServiceImplTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AnnouncementValidationService announcementValidationService;

    @InjectMocks
    private AnnouncementServiceImpl announcementService;

    @Test
    void createAnnouncement_shouldReturnAnnouncementDto() {
        Long userId = 1L;
        AnnouncementCreateRequest createRequest = new AnnouncementCreateRequest();
        createRequest.setTitle("Test Title");
        createRequest.setContent("Test Content");

        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("Test Title");

        AnnouncementDto expectedDto = new AnnouncementDto();
        expectedDto.setId(1L);
        expectedDto.setTitle("Test Title");

        when(modelMapper.map(createRequest, Announcement.class)).thenReturn(announcement);
        when(announcementRepository.save(any(Announcement.class))).thenReturn(announcement);
        when(modelMapper.map(announcement, AnnouncementDto.class)).thenReturn(expectedDto);

        AnnouncementDto result = announcementService.createAnnouncement(createRequest, userId);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getTitle(), result.getTitle());

        verify(announcementValidationService, times(1)).validateCreateRequest(any(AnnouncementCreateRequest.class));
        verify(announcementRepository, times(1)).save(any(Announcement.class));
    }

    @Test
    void updateAnnouncement_shouldReturnUpdatedDto() {
        Long announcementId = 1L;
        Long userId = 1L;
        AnnouncementUpdateRequest updateRequest = new AnnouncementUpdateRequest();
        updateRequest.setTitle("Updated Title");

        Announcement existingAnnouncement = new Announcement();
        existingAnnouncement.setId(announcementId);
        existingAnnouncement.setTitle("Old Title");

        AnnouncementDto expectedDto = new AnnouncementDto();
        expectedDto.setId(announcementId);
        expectedDto.setTitle("Updated Title");

        when(announcementRepository.findById(announcementId)).thenReturn(Optional.of(existingAnnouncement));
        when(announcementRepository.save(any(Announcement.class))).thenReturn(existingAnnouncement);
        when(modelMapper.map(existingAnnouncement, AnnouncementDto.class)).thenReturn(expectedDto);

        AnnouncementDto result = announcementService.updateAnnouncement(announcementId, updateRequest, userId);

        assertNotNull(result);
        assertEquals(expectedDto.getTitle(), result.getTitle());

        verify(announcementRepository, times(1)).findById(announcementId);
        verify(announcementRepository, times(1)).save(any(Announcement.class));
        verify(modelMapper, times(1)).map(any(AnnouncementUpdateRequest.class), eq(existingAnnouncement));
    }
}