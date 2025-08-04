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
│   │   ├── MessageContent.java
│   │   └── SystemMessage.java
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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("email_templates")
public class EmailTemplate {
    @Id
    private Long id;
    
    @Indexed
    private String name; // Template name for identification
    
    @Indexed
    private String code; // Unique template code
    
    private String subject; // Email subject template
    
    private String content; // Email content template (HTML/Text)
    
    private String templateType; // HTML, TEXT, FREEMARKER
    
    private String category; // WELCOME, NOTIFICATION, REMINDER, etc.
    
    private String description;
    
    private String variables; // JSON string of available template variables
    
    private boolean isDefault; // Whether this is a default system template
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
}
```

#### EmailLog Entity
```java
package com.example.demo.communication.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("email_logs")
public class EmailLog {
    @Id
    private Long id;
    
    @Indexed
    private String toEmail;
    
    private String ccEmails; // Comma-separated CC emails
    
    private String bccEmails; // Comma-separated BCC emails
    
    private String subject;
    
    private String content;
    
    @Indexed
    private String templateCode; // Template used (if any)
    
    @Indexed
    private String status; // PENDING, SENT, FAILED, BOUNCED
    
    private String errorMessage; // Error details if failed
    
    private Integer retryCount;
    
    private LocalDateTime sentAt;
    
    private LocalDateTime createdAt;
    
    @Indexed
    private Long sentBy; // User who sent the email
    
    private String messageId; // Email provider message ID
    
    private String priority; // HIGH, NORMAL, LOW
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
```## 
Chat System

### Chat Entity Classes

#### ChatMessage Entity
```java
package com.example.demo.communication.chat.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("chat_messages")
public class ChatMessage {
    @Id
    private Long id;
    
    @Indexed
    private Long roomId; // Chat room ID
    
    @Indexed
    private Long senderId; // Sender user ID
    
    private String content; // Message content
    
    @Indexed
    private String messageType; // TEXT, IMAGE, FILE, SYSTEM
    
    private String attachmentUrl; // URL for file/image attachments
    
    private String attachmentName; // Original file name
    
    private Long attachmentSize; // File size in bytes
    
    private boolean isEdited;
    
    private LocalDateTime editedAt;
    
    private boolean isDeleted;
    
    private LocalDateTime deletedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Transient fields for display
    private transient String senderName;
    private transient String senderAvatar;
    private transient boolean isRead; // Whether current user has read this message
}
```

#### ChatRoom Entity
```java
package com.example.demo.communication.chat.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("chat_rooms")
public class ChatRoom {
    @Id
    private Long id;
    
    @Indexed
    private String name; // Room name (for group chats)
    
    @Indexed
    private String type; // DIRECT, GROUP, CHANNEL
    
    private String description;
    
    private String avatarUrl;
    
    @Indexed
    private Long createdBy; // User who created the room
    
    private boolean isPrivate; // Whether room is private
    
    private boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastMessageAt;
    
    private Long lastMessageId;
    
    // Transient fields
    private transient Set<Long> participantIds;
    private transient Long unreadCount; // For current user
    private transient String lastMessageContent;
    private transient String lastMessageSender;
}
```

#### ChatParticipant Entity
```java
package com.example.demo.communication.chat.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("chat_participants")
public class ChatParticipant {
    @Id
    private String id; // Composite key: roomId:userId
    
    @Indexed
    private Long roomId;
    
    @Indexed
    private Long userId;
    
    @Indexed
    private String role; // OWNER, ADMIN, MEMBER
    
    private LocalDateTime joinedAt;
    
    private LocalDateTime lastReadAt; // Last time user read messages in this room
    
    private Long lastReadMessageId; // Last message ID read by user
    
    private boolean isMuted; // Whether user has muted this room
    
    private boolean isActive; // Whether user is still in the room
    
    private LocalDateTime leftAt; // When user left the room (if applicable)
    
    // Transient fields
    private transient String userName;
    private transient String userAvatar;
    private transient boolean isOnline;
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
    ChatRoomDto createChatRoom(String name, String type, String description, Long createdBy, List<Long> participantIds);
    
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