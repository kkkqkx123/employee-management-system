package com.example.demo.communication.announcement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnnouncementScheduledServiceTest {

    @Mock
    private AnnouncementService announcementService;

    @InjectMocks
    private AnnouncementScheduledService announcementScheduledService;

    @Test
    void cleanupExpiredAnnouncements_shouldCallService() {
        when(announcementService.cleanupExpiredAnnouncements()).thenReturn(5);

        announcementScheduledService.cleanupExpiredAnnouncements();

        verify(announcementService, times(1)).cleanupExpiredAnnouncements();
    }
}