package com.example.demo.communication.email.repository;

import com.example.demo.communication.email.entity.EmailTemplate;
import com.example.demo.communication.email.entity.TemplateCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    
    /**
     * Find template by unique code
     * @param code Template code
     * @return Optional EmailTemplate
     */
    Optional<EmailTemplate> findByCode(String code);
    
    /**
     * Find templates by category
     * @param category Template category
     * @return List of templates
     */
    List<EmailTemplate> findByCategory(TemplateCategory category);
    
    /**
     * Find enabled templates
     * @param enabled Whether template is enabled
     * @return List of enabled templates
     */
    List<EmailTemplate> findByEnabled(boolean enabled);
    
    /**
     * Find templates by category and enabled status
     * @param category Template category
     * @param enabled Whether template is enabled
     * @return List of templates
     */
    List<EmailTemplate> findByCategoryAndEnabled(TemplateCategory category, boolean enabled);
    
    /**
     * Find default template for a category
     * @param category Template category
     * @return Optional default template
     */
    Optional<EmailTemplate> findByCategoryAndIsDefaultTrue(TemplateCategory category);
    
    /**
     * Search templates by name or description
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Page of matching templates
     */
    @Query("SELECT t FROM EmailTemplate t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<EmailTemplate> searchTemplates(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Check if template code exists
     * @param code Template code
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Find templates created by user
     * @param createdBy User ID
     * @param pageable Pagination parameters
     * @return Page of templates
     */
    Page<EmailTemplate> findByCreatedBy(Long createdBy, Pageable pageable);
}