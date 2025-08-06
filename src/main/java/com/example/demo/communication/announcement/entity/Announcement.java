package com.example.demo.communication.announcement.entity;

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
import java.time.LocalDate;

@Entity
@Table(name = "announcements", indexes = {
    @Index(name = "idx_announcement_author_id", columnList = "author_id"),
    @Index(name = "idx_announcement_target_audience", columnList = "target_audience"),
    @Index(name = "idx_announcement_department_id", columnList = "department_id"),
    @Index(name = "idx_announcement_published", columnList = "published"),
    @Index(name = "idx_announcement_publish_date", columnList = "publish_date")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private Long authorId; // User who created the announcement

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", length = 50)
    private AnnouncementTarget targetAudience; // ALL, DEPARTMENT, ROLE

    @Column(name = "department_id") // if target is a specific department
    private Long departmentId;

    @Column(name = "role_name", length = 100) // if target is a specific role
    private String roleName;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "priority", length = 20)
    @Enumerated(EnumType.STRING)
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;

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
    
    public enum AnnouncementPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}