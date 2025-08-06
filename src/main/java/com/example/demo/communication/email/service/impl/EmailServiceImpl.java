package com.example.demo.communication.email.service.impl;

import com.example.demo.communication.email.dto.EmailRequest;
import com.example.demo.communication.email.dto.BulkEmailRequest;
import com.example.demo.communication.email.dto.EmailLogDto;
import com.example.demo.communication.email.dto.EmailStatisticsDto;
import com.example.demo.communication.email.entity.EmailLog;
import com.example.demo.communication.email.entity.EmailStatus;
import com.example.demo.communication.email.entity.EmailPriority;
import com.example.demo.communication.email.exception.EmailSendingException;
import com.example.demo.communication.email.exception.TemplateNotFoundException;
import com.example.demo.communication.email.exception.EmailLogNotFoundException;
import com.example.demo.communication.email.repository.EmailLogRepository;
import com.example.demo.communication.email.repository.EmailTemplateRepository;
import com.example.demo.communication.email.service.EmailService;
import com.example.demo.communication.email.service.EmailTemplateService;
import com.example.demo.communication.email.util.EmailTemplateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTemplateService emailTemplateService;
    private final EmailTemplateProcessor templateProcessor;
    
    @Override
    @Async
    public CompletableFuture<Void> sendEmail(EmailRequest emailRequest) {
        try {
            // Create and send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(emailRequest.getToEmail());
            if (emailRequest.getCcEmails() != null && !emailRequest.getCcEmails().isEmpty()) {
                helper.setCc(emailRequest.getCcEmails().toArray(new String[0]));
            }
            if (emailRequest.getBccEmails() != null && !emailRequest.getBccEmails().isEmpty()) {
                helper.setBcc(emailRequest.getBccEmails().toArray(new String[0]));
            }
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getContent(), emailRequest.isHtml());
            
            mailSender.send(message);
            
            // Log email
            EmailLog emailLog = EmailLog.builder()
                    .toEmail(emailRequest.getToEmail())
                    .ccEmails(emailRequest.getCcEmails() != null ? 
                            String.join(",", emailRequest.getCcEmails()) : null)
                    .bccEmails(emailRequest.getBccEmails() != null ? 
                            String.join(",", emailRequest.getBccEmails()) : null)
                    .subject(emailRequest.getSubject())
                    .content(emailRequest.getContent())
                    .status(EmailStatus.SENT)
                    .sentAt(Instant.now())
                    .sentBy(emailRequest.getSentBy())
                    .priority(emailRequest.getPriority())
                    .build();
            
            emailLogRepository.save(emailLog);
            
            log.info("Email sent successfully to: {}", emailRequest.getToEmail());
            return CompletableFuture.completedFuture(null);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", emailRequest.getToEmail(), e);
            throw new EmailSendingException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}", emailRequest.getToEmail(), e);
            throw new EmailSendingException("Unexpected error while sending email", e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<Void> sendTemplatedEmail(String toEmail, String templateCode, Map<String, Object> variables) {
        try {
            // Get template
            var templateDto = emailTemplateService.getTemplateByCode(templateCode);
            
            // Process template
            String subject = templateProcessor.processTemplate(templateDto.getSubject(), variables);
            String content = templateProcessor.processTemplate(templateDto.getContent(), variables);
            
            // Create and send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true); // HTML content
            
            mailSender.send(message);
            
            // Log email
            EmailLog emailLog = EmailLog.builder()
                    .toEmail(toEmail)
                    .subject(subject)
                    .content(content)
                    .templateCode(templateCode)
                    .status(EmailStatus.SENT)
                    .sentAt(Instant.now())
                    .priority(EmailPriority.NORMAL)
                    .build();
            
            emailLogRepository.save(emailLog);
            
            log.info("Templated email sent successfully to: {} using template: {}", toEmail, templateCode);
            return CompletableFuture.completedFuture(null);
            
        } catch (TemplateNotFoundException e) {
            log.error("Template not found: {}", templateCode, e);
            throw e;
        } catch (MessagingException e) {
            log.error("Failed to send templated email to: {} using template: {}", toEmail, templateCode, e);
            throw new EmailSendingException("Failed to send templated email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending templated email to: {} using template: {}", toEmail, templateCode, e);
            throw new EmailSendingException("Unexpected error while sending templated email", e);
        }
    }
    
    @Override
    @Async
    public CompletableFuture<Void> sendBulkEmails(BulkEmailRequest bulkEmailRequest) {
        // Implementation for bulk emails
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    @Async
    public CompletableFuture<Void> sendBulkTemplatedEmails(List<String> recipients, String templateCode, Map<String, Object> variables) {
        // Implementation for bulk templated emails
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public Page<EmailLogDto> getEmailLogs(Pageable pageable) {
        // Implementation for getting email logs
        return null;
    }
    
    @Override
    public Page<EmailLogDto> getEmailLogsByRecipient(String toEmail, Pageable pageable) {
        // Implementation for getting email logs by recipient
        return null;
    }
    
    @Override
    public Page<EmailLogDto> getEmailLogsByStatus(String status, Pageable pageable) {
        // Implementation for getting email logs by status
        return null;
    }
    
    @Override
    public Page<EmailLogDto> getEmailLogsByTemplate(String templateCode, Pageable pageable) {
        // Implementation for getting email logs by template
        return null;
    }
    
    @Override
    @Async
    public CompletableFuture<Void> retryFailedEmail(Long emailLogId) {
        // Implementation for retrying failed email
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public EmailStatisticsDto getEmailStatistics() {
        // Implementation for getting email statistics
        return null;
    }
}