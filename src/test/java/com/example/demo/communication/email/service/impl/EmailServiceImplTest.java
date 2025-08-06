package com.example.demo.communication.email.service.impl;

import com.example.demo.communication.email.dto.EmailRequest;
import com.example.demo.communication.email.entity.EmailTemplate;
import com.example.demo.communication.email.repository.EmailLogRepository;
import com.example.demo.communication.email.repository.EmailTemplateRepository;
import com.example.demo.communication.email.service.EmailTemplateService;
import com.example.demo.communication.email.util.EmailTemplateProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailLogRepository emailLogRepository;

    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    @Mock
    private EmailTemplateProcessor templateProcessor;
    
    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendEmail_shouldSendEmailAndLogIt() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo("test@example.com");
        emailRequest.setSubject("Test Subject");
        emailRequest.setContent("Test Content");

        emailService.sendEmail(emailRequest);

        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
        verify(emailLogRepository).save(any());
    }

    @Test
    void sendTemplatedEmail_shouldProcessAndSendEmail() {
        String to = "test@example.com";
        String templateCode = "test-template";
        
        EmailTemplate template = new EmailTemplate();
        template.setSubject("Subject: {{title}}");
        template.setContent("Hello, {{name}}");

        when(emailTemplateService.getTemplateByCode(templateCode)).thenReturn(template);
        when(templateProcessor.processTemplate(any(), any())).thenReturn("Processed Content");

        CompletableFuture<Void> future = emailService.sendTemplatedEmail(to, templateCode, Collections.emptyMap());

        future.join(); // Wait for async method to complete

        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
        verify(emailLogRepository).save(any());
    }
}