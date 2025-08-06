# Communication System Implementation

## Overview
This document provides detailed implementation specifications for the Communication System module, which includes Email Management and Chat/Notification functionality. This module handles template-based email sending, real-time chat, and system notifications.

## Package Structure
```
com.example.demo.communication/
├── email/
│   ├── entity/
│   │   ├── EmailTemplate.java
│   │   └── EmailLog.java
│   ├── service/
│   │   ├── EmailService.java
│   │   ├── EmailTemplateService.java
│   │   └── impl/
│   │       ├── EmailServiceImpl.java
│   │       └── EmailTemplateServiceImpl.java
│   ├── controller/
│   │   └── EmailController.java
│   ├── dto/
│   │   ├── EmailRequest.java
│   │   ├── BulkEmailRequest.java
│   │   ├── EmailTemplateDto.java
│   │   └── EmailLogDto.java
│   └── util/
│       └── EmailTemplateProcessor.java
├── chat/
│   ├── entity/
│   │   ├── ChatMessage.java
│   │   ├── ChatRoom.java
│   │   └── ChatParticipant.java
│   ├── service/
│   │   ├── ChatService.java
│   │   └── impl/
│   │       └── ChatServiceImpl.java
│   ├── controller/
│   │   └── ChatController.java
│   ├── dto/
│   │   ├── ChatMessageDto.java
│   │   ├── ChatRoomDto.java
│   │   └── ChatParticipantDto.java
│   └── websocket/
│       ├── ChatWebSocketHandler.java
│       └── WebSocketConfig.java
├── notification/
│   ├── entity/
│   │   └── Notification.java // Based on the single-table model in database-design.md
│   ├── service/
│   │   ├── NotificationService.java
│   │   └── impl/
│   │       └── NotificationServiceImpl.java
│   ├── controller/
│   │   └── NotificationController.java
│   ├── dto/
│   │   ├── NotificationDto.java
│   │   ├── NotificationCreateRequest.java
│   │   └── NotificationMarkReadRequest.java
│   └── websocket/
│       └── NotificationWebSocketHandler.java
├── announcement/
│   ├── controller/
│   │   └── AnnouncementController.java
│   ├── dto/
│   │   ├── AnnouncementCreateRequest.java
│   │   ├── AnnouncementDto.java
│   │   ├── AnnouncementStatisticsDto.java
│   │   └── AnnouncementUpdateRequest.java
│   ├── entity/
│   │   ├── Announcement.java
│   │   └── AnnouncementTarget.java
│   ├── repository/
│   │   └── AnnouncementRepository.java
│   └── service/
│       ├── AnnouncementScheduledService.java
│       ├── AnnouncementService.java
│       ├── AnnouncementValidationService.java
│       └── impl/
│           └── AnnouncementServiceImpl.java
└── exception/
    ├── EmailSendingException.java
    ├── TemplateNotFoundException.java
    ├── ChatRoomNotFoundException.java
    └── NotificationException.java

```

## Email Management

### Email Entity Classes

#### EmailTemplate Entity
```java
package com.example.demo.communication.email.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
 
public enum TemplateType {
    HTML,
    TEXT,
    MIXED
}
 
public enum TemplateCategory {
    WELCOME,
    NOTIFICATION,
    REMINDER,
    MARKETING,
    PASSWORD_RESET
}

@Entity
@Table(name = "email_templates", indexes = {
    @Index(name = "idx_emailtemplate_code", columnList = "code", unique = true),
    @Index(name = "idx_emailtemplate_category", columnList = "category")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name; // Template name for identification
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code; // Unique template code
    
    @Column(name = "subject", nullable = false, length = 255)
    private String subject; // Email subject template
    
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // Email content template (HTML/Text)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 20)
    private TemplateType templateType; // Template type (e.g., HTML, TEXT, MIXED)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private TemplateCategory category; // WELCOME, NOTIFICATION, REMINDER, etc.
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Lob
    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables; // JSON string of available template variables
    
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
    
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
}
```

#### EmailLog Entity
```java
package com.example.demo.communication.email.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

public enum EmailStatus {
    PENDING,
    SENT,
    FAILED,
    BOUNCED,
    DELIVERED,
    OPENED,
    CLICKED
}
 
public enum EmailPriority {
    HIGH,
    NORMAL,
    LOW
}

@Entity
@Table(name = "email_logs", indexes = {
    @Index(name = "idx_emaillog_to_email", columnList = "to_email"),
    @Index(name = "idx_emaillog_status", columnList = "status"),
    @Index(name = "idx_emaillog_template_code", columnList = "template_code"),
    @Index(name = "idx_emaillog_sent_by", columnList = "sent_by")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "to_email", nullable = false)
    private String toEmail;
    
    @Column(name = "cc_emails", length = 1000)
    private String ccEmails; // Comma-separated CC emails
    
    @Column(name = "bcc_emails", length = 1000)
    private String bccEmails; // Comma-separated BCC emails
    
    @Column(name = "subject", nullable = false)
    private String subject;
    
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "template_code", length = 50)
    private String templateCode; // Template used (if any)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailStatus status; // PENDING, SENT, FAILED, BOUNCED
    
    @Column(name = "error_message", length = 2000)
    private String errorMessage; // Error details if failed
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "sent_at")
    private Instant sentAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "sent_by")
    private Long sentBy; // User who sent the email
    
    @Column(name = "message_id", length = 255)
    private String messageId; // Email provider message ID
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private EmailPriority priority; // HIGH, NORMAL, LOW
}
```

### Email Service Interface
```java
package com.example.demo.communication.email.service;

import com.example.demo.communication.email.dto.EmailRequest;
import com.example.demo.communication.email.dto.BulkEmailRequest;
import com.example.demo.communication.email.dto.EmailLogDto;
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
```

## Chat System

### Chat Entity Classes

### Enum Definitions

#### ChatRoomType Enum
```java
package com.example.demo.communication.chat.entity;

public enum ChatRoomType {
    DIRECT, // One-on-one chat
    GROUP,  // Multi-user group chat
    CHANNEL // Broadcast-style channel
}
```

#### ChatParticipantRole Enum
```java
package com.example.demo.communication.chat.entity;

public enum ChatParticipantRole {
    OWNER,
    ADMIN,
    MEMBER
}
```
#### ChatMessage Entity
```java
package com.example.demo.communication.chat.entity;
 
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
 
import java.time.Instant;

public enum ChatMessageType {
    TEXT,
    IMAGE,
    FILE,
    SYSTEM // 系统消息, e.g., "User A has joined the room"
}
 
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chatmessage_room_id", columnList = "room_id"),
    @Index(name = "idx_chatmessage_sender_id", columnList = "sender_id"),
    @Index(name = "idx_chatmessage_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;
  
    @Column(name = "sender_id", nullable = false)
    private Long senderId;         // Message sender
  
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;        // Message content
  
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private ChatMessageType messageType;    // TEXT, IMAGE, FILE, SYSTEM
  
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;     // 时区无关的时间戳
  
    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;
  
    @Column(name = "edited_at")
    private Instant editedAt;      // 时区无关的时间戳
  
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
  
    @Column(name = "deleted_at")
    private Instant deletedAt;     // 时区无关的时间戳
    
    // Transient fields for display
    private transient String senderName;
    private transient String senderAvatar;
    private transient boolean isRead; // Whether current user has read this message
    
}
```

#### ChatRoom Entity
```java
package com.example.demo.communication.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_rooms", indexes = {
    @Index(name = "idx_chatroom_type", columnList = "type"),
    @Index(name = "idx_chatroom_created_by", columnList = "created_by")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", length = 100)
    private String name; // Room name (for group chats)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChatRoomType type; // DIRECT, GROUP, CHANNEL
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy; // User who created the room
    
    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = false;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "last_message_at")
    private Instant lastMessageAt;
    
    @Column(name = "last_message_id")
    private Long lastMessageId; // Stored in Redis, synced periodically
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatParticipant> participants = new HashSet<>();

    // Transient fields for DTO mapping
    @Transient
    private Long unreadCount;
    @Transient
    private String lastMessageContent;
    @Transient
    private String lastMessageSender;
}
```

#### ChatParticipant Entity
```java
package com.example.demo.communication.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "chat_participants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "user_id"}, name = "uk_participant_room_user")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ChatParticipantRole role; // OWNER, ADMIN, MEMBER
    
    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;
    
    @Column(name = "last_read_at")
    private Instant lastReadAt;
    
    @Column(name = "last_read_message_id")
    private Long lastReadMessageId; // Stored in Redis, synced periodically
    
    @Column(name = "is_muted", nullable = false)
    private boolean isMuted = false;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "left_at")
    private Instant leftAt;
    
    // Transient fields for DTO mapping
    @Transient
    private String userName;
    @Transient
    private String userAvatar;
    @Transient
    private boolean isOnline;
}
```

### Chat Service Interface
```java
package com.example.demo.communication.chat.service;

import com.example.demo.communication.chat.dto.ChatMessageDto;
import com.example.demo.communication.chat.dto.ChatRoomDto;
import com.example.demo.communication.chat.dto.ChatParticipantDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    
    /**
     * Create a new chat room
     * @param name Room name
     * @param type Room type (DIRECT, GROUP, CHANNEL)
     * @param description Room description
     * @param createdBy Creator user ID
     * @param participantIds Initial participant user IDs
     * @return Created chat room DTO
     * @throws ChatRoomCreationException if room creation fails
     */
    ChatRoomDto createChatRoom(String name, ChatRoomType type, String description, Long createdBy, List<Long> participantIds);
    
    /**
     * Get chat room by ID
     * @param roomId Room ID
     * @param userId Current user ID (for permission check)
     * @return Chat room DTO
     * @throws ChatRoomNotFoundException if room not found
     * @throws AccessDeniedException if user doesn't have access
     */
    ChatRoomDto getChatRoom(Long roomId, Long userId);
    
    /**
     * Get user's chat rooms
     * @param userId User ID
     * @return List of chat room DTOs
     */
    List<ChatRoomDto> getUserChatRooms(Long userId);
    
    /**
     * Send message to chat room
     * @param roomId Room ID
     * @param senderId Sender user ID
     * @param content Message content
     * @param messageType Message type
     * @return Created message DTO
     * @throws ChatRoomNotFoundException if room not found
     * @throws AccessDeniedException if user doesn't have access
     */
    ChatMessageDto sendMessage(Long roomId, Long senderId, String content, String messageType);
    
    /**
     * Get chat messages for room
     * @param roomId Room ID
     * @param userId Current user ID (for permission check)
     * @param pageable Pagination parameters
     * @return Page of message DTOs
     * @throws ChatRoomNotFoundException if room not found
     * @throws AccessDeniedException if user doesn't have access
     */
    Page<ChatMessageDto> getChatMessages(Long roomId, Long userId, Pageable pageable);
    
    /**
     * Edit message
     * @param messageId Message ID
     * @param userId User ID (must be message sender)
     * @param newContent New message content
     * @return Updated message DTO
     * @throws ChatMessageNotFoundException if message not found
     * @throws AccessDeniedException if user is not the sender
     */
    ChatMessageDto editMessage(Long messageId, Long userId, String newContent);
    
    /**
     * Delete message
     * @param messageId Message ID
     * @param userId User ID (must be message sender or room admin)
     * @throws ChatMessageNotFoundException if message not found
     * @throws AccessDeniedException if user doesn't have permission
     */
    void deleteMessage(Long messageId, Long userId);
    
    /**
     * Add participant to chat room
     * @param roomId Room ID
     * @param userId User ID to add
     * @param addedBy User ID who is adding (must have permission)
     * @return Chat participant DTO
     * @throws ChatRoomNotFoundException if room not found
     * @throws AccessDeniedException if addedBy user doesn't have permission
     */
    ChatParticipantDto addParticipant(Long roomId, Long userId, Long addedBy);
    
    /**
     * Remove participant from chat room
     * @param roomId Room ID
     * @param userId User ID to remove
     * @param removedBy User ID who is removing (must have permission)
     * @throws ChatRoomNotFoundException if room not found
     * @throws AccessDeniedException if removedBy user doesn't have permission
     */
    void removeParticipant(Long roomId, Long userId, Long removedBy);
    
    /**
     * Mark messages as read
     * @param roomId Room ID
     * @param userId User ID
     * @param lastReadMessageId Last message ID read
     */
    void markMessagesAsRead(Long roomId, Long userId, Long lastReadMessageId);
    
    /**
     * Get unread message count for user
     * @param userId User ID
     * @return Total unread message count across all rooms
     */
    Long getUnreadMessageCount(Long userId);
    
    /**
     * Get unread message count for specific room
     * @param roomId Room ID
     * @param userId User ID
     * @return Unread message count for the room
     */
    Long getUnreadMessageCount(Long roomId, Long userId);
    
    /**
     * Search messages in room
     * @param roomId Room ID
     * @param userId Current user ID (for permission check)
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Page of matching message DTOs
     */
    Page<ChatMessageDto> searchMessages(Long roomId, Long userId, String searchTerm, Pageable pageable);
}
```

## Notification & Announcement System

**Note:** The implementation of the notification system must adhere to the `Notification` entity defined in `database-design.md`. The single-table model is the authoritative design standard, superseding any previous multi-table designs.

This section covers system-wide notifications and announcements.

### Announcement Entity
```java
package com.example.demo.communication.announcement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
 
public enum AnnouncementTarget {
    ALL,          // 全体员工
    DEPARTMENT,   // 特定部门
    ROLE          // 特定角色
}

@Entity
@Table(name = "announcements")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private Long authorId; // Added author ID

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", length = 50) // e.g., ALL, DEPARTMENT, ROLE
    private AnnouncementTarget targetAudience;

    @Column(name = "department_id") // if target is a specific department
    private Long departmentId;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean published = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt; // Added updatedAt

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy; // Added updatedBy
}
```

### Notification Service Implementation
```java
package com.example.demo.communication.notification.service.impl;

import com.example.demo.communication.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    // Assume WebSocket and Email services are injected
    // private final SimpMessagingTemplate messagingTemplate;
    // private final EmailService emailService;

    @Override
    @Async // Execute asynchronously to avoid blocking the caller thread
    public void sendNotification(Long userId, String message) {
        try {
            log.info("Sending notification to user {}: {}", userId, message);
            // 1. Send via WebSocket for real-time update
            // messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", message);

            // 2. Optionally, send an email as well
            // Employee user = employeeRepository.findById(userId).orElse(null);
            // if (user != null && user.getEmail() != null) {
            //     emailService.sendSimpleMessage(user.getEmail(), "New Notification", message);
            // }
            log.info("Successfully sent notification to user {}", userId);
        } catch (Exception e) {
            // Robust error handling
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage(), e);
            // Here you could add logic to retry, or flag the notification as failed
        }
    }
}
```

### Announcement Service Implementation
```java
package com.example.demo.communication.announcement.service.impl;

import com.example.demo.communication.announcement.dto.AnnouncementDto;
import com.example.demo.communication.announcement.entity.Announcement;
import com.example.demo.communication.announcement.repository.AnnouncementRepository;
import com.example.demo.communication.announcement.service.AnnouncementService;
import com.example.demo.communication.notification.service.NotificationService;
import com.example.demo.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository; // To get target employees
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AnnouncementDto createAnnouncement(AnnouncementDto announcementDto) {
        Announcement announcement = modelMapper.map(announcementDto, Announcement.class);
        Announcement savedAnnouncement = announcementRepository.save(announcement);

        // If the announcement is published, send notifications
        if (savedAnnouncement.isPublished()) {
            notifyUsers(savedAnnouncement);
        }

        return modelMapper.map(savedAnnouncement, AnnouncementDto.class);
    }
  
    @Override
    @Transactional
    public AnnouncementDto updateAnnouncement(Long id, AnnouncementDto announcementDto) {
        Announcement existing = announcementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Announcement not found"));
      
        modelMapper.map(announcementDto, existing);
        Announcement updatedAnnouncement = announcementRepository.save(existing);

        // If the announcement is published, send notifications
        if (updatedAnnouncement.isPublished()) {
            notifyUsers(updatedAnnouncement);
        }
      
        return modelMapper.map(updatedAnnouncement, AnnouncementDto.class);
    }

    private void notifyUsers(Announcement announcement) {
        String message = "New Announcement: " + announcement.getTitle();
        List<Long> targetUserIds = getTargetUserIds(announcement);
      
        targetUserIds.forEach(userId -> notificationService.sendNotification(userId, message));
    }

    private List<Long> getTargetUserIds(Announcement announcement) {
        // Logic to determine which users should be notified based on target_audience
        // For example:
        if ("ALL".equals(announcement.getTargetAudience())) {
            return employeeRepository.findAll().stream().map(e -> e.getId()).collect(Collectors.toList());
        } else if ("DEPARTMENT".equals(announcement.getTargetAudience()) && announcement.getDepartmentId() != null) {
            // Assuming EmployeeRepository has findByDepartmentId method
            // return employeeRepository.findByDepartmentId(announcement.getDepartmentId()).stream()...
        }
        return List.of(); // return empty list if no target
    }
    // Other methods (get, delete, etc.) would be implemented here.
}
```

### Announcement Controller
```java
package com.example.demo.communication.announcement.controller;

import com.example.demo.communication.announcement.dto.AnnouncementDto;
import com.example.demo.communication.announcement.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_MANAGE')")
    public ResponseEntity<AnnouncementDto> createAnnouncement(@Valid @RequestBody AnnouncementDto announcementDto) {
        AnnouncementDto created = announcementService.createAnnouncement(announcementDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT_MANAGE')")
    public ResponseEntity<AnnouncementDto> updateAnnouncement(@PathVariable Long id, @Valid @RequestBody AnnouncementDto announcementDto) {
        AnnouncementDto updated = announcementService.updateAnnouncement(id, announcementDto);
        return ResponseEntity.ok(updated);
    }

    // Endpoints for getting/deleting announcements would be here
}
```