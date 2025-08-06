package com.example.demo.communication.chat.entity;
 
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
 
import java.time.Instant;

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
    private Instant createdAt;     // Timezone-independent timestamp
  
    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;
  
    @Column(name = "edited_at")
    private Instant editedAt;      // Timezone-independent timestamp
  
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
  
    @Column(name = "deleted_at")
    private Instant deletedAt;     // Timezone-independent timestamp
    
    // Transient fields for display
    @Transient
    private String senderName;
    @Transient
    private String senderAvatar;
    @Transient
    private boolean isRead; // Whether current user has read this message
}