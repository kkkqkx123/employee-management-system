package com.example.demo.integration;

import com.example.demo.communication.email.dto.EmailRequest;
import com.example.demo.communication.email.dto.BulkEmailRequest;
import com.example.demo.communication.email.dto.EmailTemplateDto;
import com.example.demo.communication.email.entity.EmailLog;
import com.example.demo.communication.email.entity.EmailStatus;
import com.example.demo.communication.email.entity.EmailTemplate;
import com.example.demo.communication.email.entity.TemplateType;
import com.example.demo.communication.email.repository.EmailLogRepository;
import com.example.demo.communication.email.repository.EmailTemplateRepository;
import com.example.demo.communication.email.service.EmailService;
import com.example.demo.communication.email.service.EmailTemplateService;
import com.example.demo.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import java.time.Duration;

/**
 * Integration tests for EmailService.
 * Tests email service with actual database interactions and async processing.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        emailLogRepository.deleteAll();
        emailTemplateRepository.deleteAll();
    }

    @Test
    void sendEmail_shouldLogEmailToDatabase() {
        // Given
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setToEmail("test@example.com");
        emailRequest.setSubject("Test Subject");
        emailRequest.setContent("Test Content");

        // When
        emailService.sendEmail(emailRequest);

        // Then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            List<EmailLog> emailLogs = emailLogRepository.findAll();
            assertThat(emailLogs).hasSize(1);
            
            EmailLog log = emailLogs.get(0);
            assertThat(log.getToEmail()).isEqualTo("test@example.com");
            assertThat(log.getSubject()).isEqualTo("Test Subject");
            assertThat(log.getContent()).isEqualTo("Test Content");
            assertThat(log.getStatus()).isIn(EmailStatus.SENT, EmailStatus.FAILED);
        });
    }

    @Test
    void sendTemplatedEmail_shouldProcessTemplateAndLog() {
        // Given
        // Create email template
        EmailTemplate template = new EmailTemplate();
        template.setName("Welcome Template");
        template.setCode("WELCOME");
        template.setSubject("Welcome {{name}}!");
        template.setContent("Hello {{name}}, welcome to our platform!");
        template = emailTemplateRepository.save(template);

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John Doe");

        // When
        emailService.sendTemplatedEmail(
                "john.doe@example.com",
                "WELCOME",
                variables
        );

        // Then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            List<EmailLog> emailLogs = emailLogRepository.findAll();
            assertThat(emailLogs).hasSize(1);
            
            EmailLog log = emailLogs.get(0);
            assertThat(log.getToEmail()).isEqualTo("john.doe@example.com");
            assertThat(log.getSubject()).contains("Welcome John Doe!");
            assertThat(log.getContent()).contains("Hello John Doe, welcome");
            assertThat(log.getTemplateCode()).isEqualTo("WELCOME");
        });
    }

    @Test
    void createEmailTemplate_shouldPersistToDatabase() {
        // Given
        EmailTemplateDto templateDto = new EmailTemplateDto();
        templateDto.setName("Password Reset");
        templateDto.setCode("PASSWORD_RESET");
        templateDto.setSubject("Reset Your Password");
        templateDto.setContent("Click here to reset: {{resetLink}}");
        templateDto.setTemplateType(TemplateType.HTML);

        // When
        EmailTemplateDto result = emailTemplateService.createTemplate(templateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        // Verify persistence
        EmailTemplate saved = emailTemplateRepository.findById(result.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Password Reset");
        assertThat(saved.getCode()).isEqualTo("PASSWORD_RESET");
        assertThat(saved.getTemplateType()).isEqualTo(TemplateType.HTML);
    }

    @Test
    void getTemplateByCode_shouldReturnFromDatabase() {
        // Given
        EmailTemplate template = new EmailTemplate();
        template.setName("Test Template");
        template.setCode("TEST_TEMPLATE");
        template.setSubject("Test Subject");
        template.setContent("Test Content");
        template.setTemplateType(TemplateType.TEXT);
        template = emailTemplateRepository.save(template);

        // When
        EmailTemplateDto result = emailTemplateService.getTemplateByCode("TEST_TEMPLATE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(template.getId());
        assertThat(result.getName()).isEqualTo("Test Template");
        assertThat(result.getCode()).isEqualTo("TEST_TEMPLATE");
    }

    @Test
    void sendBulkEmails_shouldProcessAllEmailsAndLog() {
        // Given
        List<String> recipients = List.of(
                "user1@example.com",
                "user2@example.com",
                "user3@example.com"
        );

        BulkEmailRequest bulkRequest = BulkEmailRequest.builder()
                .recipients(recipients)
                .subject("Bulk Email Test")
                .content("This is a bulk email test")
                .build();

        // When
        emailService.sendBulkEmails(bulkRequest);

        // Then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<EmailLog> emailLogs = emailLogRepository.findAll();
            assertThat(emailLogs).hasSize(3);
            
            List<String> loggedEmails = emailLogs.stream()
                    .map(EmailLog::getToEmail)
                    .toList();
            assertThat(loggedEmails).containsExactlyInAnyOrder(
                    "user1@example.com",
                    "user2@example.com", 
                    "user3@example.com"
            );
            
            emailLogs.forEach(log -> {
                assertThat(log.getSubject()).isEqualTo("Bulk Email Test");
                assertThat(log.getContent()).isEqualTo("This is a bulk email test");
                assertThat(log.getStatus()).isIn(EmailStatus.SENT, EmailStatus.FAILED);
            });
        });
    }

    @Test
    void getEmailStatistics_shouldReturnCorrectCounts() {
        // Given
        // Create some email logs
        EmailLog sentLog = new EmailLog();
        sentLog.setToEmail("sent@example.com");
        sentLog.setSubject("Sent Email");
        sentLog.setContent("Content");
        sentLog.setStatus(EmailStatus.SENT);
        emailLogRepository.save(sentLog);

        EmailLog failedLog = new EmailLog();
        failedLog.setToEmail("failed@example.com");
        failedLog.setSubject("Failed Email");
        failedLog.setContent("Content");
        failedLog.setStatus(EmailStatus.FAILED);
        emailLogRepository.save(failedLog);

        EmailLog pendingLog = new EmailLog();
        pendingLog.setToEmail("pending@example.com");
        pendingLog.setSubject("Pending Email");
        pendingLog.setContent("Content");
        pendingLog.setStatus(EmailStatus.PENDING);
        emailLogRepository.save(pendingLog);

        // When
        var statistics = emailService.getEmailStatistics();

        // Then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getSentEmails()).isEqualTo(1);
        assertThat(statistics.getFailedEmails()).isEqualTo(1);
        assertThat(statistics.getPendingEmails()).isEqualTo(1);
    }

    @Test
    void updateTemplate_shouldPersistChanges() {
        // Given
        EmailTemplate template = new EmailTemplate();
        template.setName("Original Template");
        template.setCode("ORIGINAL");
        template.setSubject("Original Subject");
        template.setContent("Original Content");
        template.setTemplateType(TemplateType.HTML);
        template = emailTemplateRepository.save(template);

        EmailTemplateDto updateDto = new EmailTemplateDto();
        updateDto.setName("Updated Template");
        updateDto.setSubject("Updated Subject");
        updateDto.setContent("Updated Content");
        updateDto.setTemplateType(TemplateType.TEXT);

        // When
        EmailTemplateDto result = emailTemplateService.updateTemplate(template.getId(), updateDto);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Template");
        
        // Verify persistence
        EmailTemplate updated = emailTemplateRepository.findById(template.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated Template");
        assertThat(updated.getSubject()).isEqualTo("Updated Subject");
        assertThat(updated.getContent()).isEqualTo("Updated Content");
        assertThat(updated.getTemplateType()).isEqualTo(TemplateType.TEXT);
    }
}