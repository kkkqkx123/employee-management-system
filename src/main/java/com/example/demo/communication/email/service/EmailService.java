package com.example.demo.communication.email.service;

import com.example.demo.communication.email.dto.EmailRequest;
import com.example.demo.communication.email.dto.BulkEmailRequest;
import com.example.demo.communication.email.dto.EmailLogDto;
import com.example.demo.communication.email.dto.EmailStatisticsDto;
import com.example.demo.communication.email.exception.EmailSendingException;
import com.example.demo.communication.email.exception.TemplateNotFoundException;
import com.example.demo.communication.email.exception.EmailLogNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface EmailService {
    
    /**
     * Send a single email
     * @param emailRequest Email request details
     * @return CompletableFuture for async processing
     * @throws EmailSendingException if email sending fails
     */
    CompletableFuture<Void> sendEmail(EmailRequest emailRequest);
    
    /**
     * Send templated email
     * @param toEmail Recipient email
     * @param templateCode Template code
     * @param variables Template variables
     * @return CompletableFuture for async processing
     * @throws TemplateNotFoundException if template not found
     * @throws EmailSendingException if email sending fails
     */
    CompletableFuture<Void> sendTemplatedEmail(String toEmail, String templateCode, Map<String, Object> variables);
    
    /**
     * Send bulk emails
     * @param bulkEmailRequest Bulk email request
     * @return CompletableFuture for async processing
     * @throws EmailSendingException if bulk email sending fails
     */
    CompletableFuture<Void> sendBulkEmails(BulkEmailRequest bulkEmailRequest);
    
    /**
     * Send bulk templated emails
     * @param recipients List of recipient emails
     * @param templateCode Template code
     * @param variables Template variables
     * @return CompletableFuture for async processing
     * @throws TemplateNotFoundException if template not found
     * @throws EmailSendingException if bulk email sending fails
     */
    CompletableFuture<Void> sendBulkTemplatedEmails(List<String> recipients, String templateCode, Map<String, Object> variables);
    
    /**
     * Get email logs with pagination
     * @param pageable Pagination parameters
     * @return Page of email log DTOs
     */
    Page<EmailLogDto> getEmailLogs(Pageable pageable);
    
    /**
     * Get email logs by recipient
     * @param toEmail Recipient email
     * @param pageable Pagination parameters
     * @return Page of email log DTOs
     */
    Page<EmailLogDto> getEmailLogsByRecipient(String toEmail, Pageable pageable);
    
    /**
     * Get email logs by status
     * @param status Email status
     * @param pageable Pagination parameters
     * @return Page of email log DTOs
     */
    Page<EmailLogDto> getEmailLogsByStatus(String status, Pageable pageable);
    
    /**
     * Get email logs by template
     * @param templateCode Template code
     * @param pageable Pagination parameters
     * @return Page of email log DTOs
     */
    Page<EmailLogDto> getEmailLogsByTemplate(String templateCode, Pageable pageable);
    
    /**
     * Retry failed email
     * @param emailLogId Email log ID
     * @return CompletableFuture for async processing
     * @throws EmailLogNotFoundException if email log not found
     * @throws EmailSendingException if retry fails
     */
    CompletableFuture<Void> retryFailedEmail(Long emailLogId);
    
    /**
     * Get email sending statistics
     * @return Email statistics
     */
    EmailStatisticsDto getEmailStatistics();
}