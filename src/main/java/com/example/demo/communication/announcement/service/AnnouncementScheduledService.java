package com.example.demo.communication.announcement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementScheduledService {

    private final AnnouncementService announcementService;

    /**
     * Clean up expired announcements daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredAnnouncements() {
        try {
            log.info("Starting scheduled cleanup of expired announcements");
            int cleanedUp = announcementService.cleanupExpiredAnnouncements();
            log.info("Scheduled cleanup completed. {} expired announcements processed", cleanedUp);
        } catch (Exception e) {
            log.error("Error during scheduled cleanup of expired announcements: {}", e.getMessage(), e);
        }
    }
}