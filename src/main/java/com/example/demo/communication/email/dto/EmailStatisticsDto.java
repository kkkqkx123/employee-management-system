package com.example.demo.communication.email.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatisticsDto {
    
    private long totalEmails;
    
    private long sentEmails;
    
    private long pendingEmails;
    
    private long failedEmails;
    
    private long bouncedEmails;
    
    private long deliveredEmails;
    
    private long openedEmails;
    
    private long clickedEmails;
    
    private double successRate; // Percentage of successfully sent emails
    
    private double deliveryRate; // Percentage of delivered emails
    
    private double openRate; // Percentage of opened emails
    
    private double clickRate; // Percentage of clicked emails
    
    private Map<String, Long> emailsByTemplate; // Email count by template
    
    private Map<String, Long> emailsByStatus; // Email count by status
    
    private Instant periodStart; // Statistics period start
    
    private Instant periodEnd; // Statistics period end
    
    private long averageRetryCount; // Average retry count for failed emails
}