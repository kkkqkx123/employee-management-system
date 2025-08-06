package com.example.demo.communication.announcement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementStatisticsDto {
    private long totalAnnouncements;
    private long publishedAnnouncements;
    private long expiredAnnouncements;
    private long draftAnnouncements;
}