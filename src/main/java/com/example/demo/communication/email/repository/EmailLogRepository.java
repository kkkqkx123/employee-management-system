package com.example.demo.communication.email.repository;

import com.example.demo.communication.email.entity.EmailLog;
import com.example.demo.communication.email.entity.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    
    /**
     * Find email logs by recipient
     * @param toEmail Recipient email
     * @param pageable Pagination parameters
     * @return Page of email logs
     */
    Page<EmailLog> findByToEmail(String toEmail, Pageable pageable);
    
    /**
     * Find email logs by status
     * @param status Email status
     * @param pageable Pagination parameters
     * @return Page of email logs
     */
    Page<EmailLog> findByStatus(EmailStatus status, Pageable pageable);
    
    /**
     * Find email logs by template code
     * @param templateCode Template code
     * @param pageable Pagination parameters
     * @return Page of email logs
     */
    Page<EmailLog> findByTemplateCode(String templateCode, Pageable pageable);
    
    /**
     * Find email logs by sender
     * @param sentBy Sender user ID
     * @param pageable Pagination parameters
     * @return Page of email logs
     */
    Page<EmailLog> findBySentBy(Long sentBy, Pageable pageable);
    
    /**
     * Find failed emails for retry
     * @param status Email status (FAILED)
     * @param maxRetryCount Maximum retry count
     * @return List of failed emails
     */
    List<EmailLog> findByStatusAndRetryCountLessThan(EmailStatus status, Integer maxRetryCount);
    
    /**
     * Find emails sent within date range
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of email logs
     */
    @Query("SELECT e FROM EmailLog e WHERE e.sentAt BETWEEN :startDate AND :endDate")
    Page<EmailLog> findBySentAtBetween(@Param("startDate") Instant startDate, 
                                       @Param("endDate") Instant endDate, 
                                       Pageable pageable);
    
    /**
     * Count emails by status
     * @param status Email status
     * @return Count of emails
     */
    long countByStatus(EmailStatus status);
    
    /**
     * Count emails sent by user
     * @param sentBy User ID
     * @return Count of emails
     */
    long countBySentBy(Long sentBy);
    
    /**
     * Count emails using template
     * @param templateCode Template code
     * @return Count of emails
     */
    long countByTemplateCode(String templateCode);
    
    /**
     * Find recent email logs
     * @param limit Number of recent logs
     * @return List of recent email logs
     */
    @Query("SELECT e FROM EmailLog e ORDER BY e.createdAt DESC")
    List<EmailLog> findRecentEmailLogs(Pageable pageable);
    
    /**
     * Get email statistics by date range
     * @param startDate Start date
     * @param endDate End date
     * @return Email statistics
     */
    @Query("SELECT e.status, COUNT(e) FROM EmailLog e WHERE e.createdAt BETWEEN :startDate AND :endDate GROUP BY e.status")
    List<Object[]> getEmailStatisticsByDateRange(@Param("startDate") Instant startDate, 
                                                  @Param("endDate") Instant endDate);
}