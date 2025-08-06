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
    @Builder.Default
    private boolean isPrivate = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
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
    @Builder.Default
    private Set<ChatParticipant> participants = new HashSet<>();

    // Transient fields for DTO mapping
    @Transient
    private Long unreadCount;
    @Transient
    private String lastMessageContent;
    @Transient
    private String lastMessageSender;
}