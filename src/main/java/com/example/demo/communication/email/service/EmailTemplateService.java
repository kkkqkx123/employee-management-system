package com.example.demo.communication.email.service;

import com.example.demo.communication.email.dto.EmailTemplateDto;
import com.example.demo.communication.email.entity.TemplateCategory;
import com.example.demo.communication.email.exception.TemplateNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface EmailTemplateService {
    
    /**
     * Create a new email template
     * @param templateDto Template data
     * @return Created template DTO
     */
    EmailTemplateDto createTemplate(EmailTemplateDto templateDto);
    
    /**
     * Update an existing email template
     * @param id Template ID
     * @param templateDto Updated template data
     * @return Updated template DTO
     * @throws TemplateNotFoundException if template not found
     */
    EmailTemplateDto updateTemplate(Long id, EmailTemplateDto templateDto);
    
    /**
     * Get template by ID
     * @param id Template ID
     * @return Template DTO
     * @throws TemplateNotFoundException if template not found
     */
    EmailTemplateDto getTemplate(Long id);
    
    /**
     * Get template by code
     * @param code Template code
     * @return Template DTO
     * @throws TemplateNotFoundException if template not found
     */
    EmailTemplateDto getTemplateByCode(String code);
    
    /**
     * Get all templates with pagination
     * @param pageable Pagination parameters
     * @return Page of template DTOs
     */
    Page<EmailTemplateDto> getAllTemplates(Pageable pageable);
    
    /**
     * Get templates by category
     * @param category Template category
     * @return List of template DTOs
     */
    List<EmailTemplateDto> getTemplatesByCategory(TemplateCategory category);
    
    /**
     * Get enabled templates
     * @return List of enabled template DTOs
     */
    List<EmailTemplateDto> getEnabledTemplates();
    
    /**
     * Search templates by name or description
     * @param searchTerm Search term
     * @param pageable Pagination parameters
     * @return Page of matching template DTOs
     */
    Page<EmailTemplateDto> searchTemplates(String searchTerm, Pageable pageable);
    
    /**
     * Delete template
     * @param id Template ID
     * @throws TemplateNotFoundException if template not found
     */
    void deleteTemplate(Long id);
    
    /**
     * Enable/disable template
     * @param id Template ID
     * @param enabled Whether to enable or disable
     * @throws TemplateNotFoundException if template not found
     */
    void toggleTemplateStatus(Long id, boolean enabled);
    
    /**
     * Set template as default for category
     * @param id Template ID
     * @throws TemplateNotFoundException if template not found
     */
    void setAsDefault(Long id);
    
    /**
     * Process template with variables
     * @param templateCode Template code
     * @param variables Template variables
     * @return Processed template content
     * @throws TemplateNotFoundException if template not found
     */
    String processTemplate(String templateCode, Map<String, Object> variables);
    
    /**
     * Validate template syntax
     * @param content Template content
     * @return Validation result
     */
    boolean validateTemplate(String content);
    
    /**
     * Get template variables
     * @param templateCode Template code
     * @return List of available variables
     * @throws TemplateNotFoundException if template not found
     */
    List<String> getTemplateVariables(String templateCode);
}