package com.example.demo.communication.announcement.repository;

import com.example.demo.communication.announcement.entity.Announcement;
import com.example.demo.communication.announcement.entity.AnnouncementTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    /**
     * Find published announcements
     */
    Page<Announcement> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Find announcements by author
     */
    Page<Announcement> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    
    /**
     * Find announcements by target audience
     */
    Page<Announcement> findByTargetAudienceOrderByCreatedAtDesc(AnnouncementTarget targetAudience, Pageable pageable);
    
    /**
     * Find announcements for a specific department
     */
    Page<Announcement> findByTargetAudienceAndDepartmentIdAndPublishedTrueOrderByCreatedAtDesc(
            AnnouncementTarget targetAudience, Long departmentId, Pageable pageable);
    
    /**
     * Find active announcements (published and not expired)
     */
    @Query("SELECT a FROM Announcement a WHERE a.published = true " +
           "AND (a.expiryDate IS NULL OR a.expiryDate >= :currentDate) " +
           "ORDER BY a.createdAt DESC")
    Page<Announcement> findActiveAnnouncements(@Param("currentDate") LocalDate currentDate, Pageable pageable);
    
    /**
     * Find announcements visible to user based on department and role
     */
    @Query("SELECT a FROM Announcement a WHERE a.published = true " +
           "AND (a.expiryDate IS NULL OR a.expiryDate >= :currentDate) " +
           "AND (a.targetAudience = 'ALL' " +
           "     OR (a.targetAudience = 'DEPARTMENT' AND a.departmentId = :departmentId) " +
           "     OR (a.targetAudience = 'ROLE' AND a.roleName = :roleName)) " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    Page<Announcement> findAnnouncementsForUser(
            @Param("currentDate") LocalDate currentDate,
            @Param("departmentId") Long departmentId,
            @Param("roleName") String roleName,
            Pageable pageable);
    
    /**
     * Find expired announcements
     */
    @Query("SELECT a FROM Announcement a WHERE a.expiryDate IS NOT NULL AND a.expiryDate < :currentDate")
    List<Announcement> findExpiredAnnouncements(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Count published announcements
     */
    long countByPublishedTrue();
    
    /**
     * Count announcements by author
     */
    long countByAuthorId(Long authorId);
}