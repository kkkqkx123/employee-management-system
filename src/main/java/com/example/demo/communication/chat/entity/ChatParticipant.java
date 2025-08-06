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
    @Builder.Default
    private boolean isMuted = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
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