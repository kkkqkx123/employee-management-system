package com.example.demo.communication.email.service.impl;

import com.example.demo.communication.email.dto.EmailTemplateDto;
import com.example.demo.communication.email.entity.EmailTemplate;
import com.example.demo.communication.email.entity.TemplateCategory;
import com.example.demo.communication.email.exception.TemplateNotFoundException;
import com.example.demo.communication.email.repository.EmailTemplateRepository;
import com.example.demo.communication.email.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {
    
    private final EmailTemplateRepository emailTemplateRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public EmailTemplateDto createTemplate(EmailTemplateDto templateDto) {
        EmailTemplate template = modelMapper.map(templateDto, EmailTemplate.class);
        EmailTemplate savedTemplate = emailTemplateRepository.save(template);
        return modelMapper.map(savedTemplate, EmailTemplateDto.class);
    }
    
    @Override
    public EmailTemplateDto updateTemplate(Long id, EmailTemplateDto templateDto) {
        EmailTemplate existingTemplate = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with id: " + id));
        
        // Update fields
        existingTemplate.setName(templateDto.getName());
        existingTemplate.setCode(templateDto.getCode());
        existingTemplate.setSubject(templateDto.getSubject());
        existingTemplate.setContent(templateDto.getContent());
        existingTemplate.setTemplateType(templateDto.getTemplateType());
        existingTemplate.setCategory(templateDto.getCategory());
        existingTemplate.setDescription(templateDto.getDescription());
        existingTemplate.setDefault(templateDto.isDefault());
        existingTemplate.setEnabled(templateDto.isEnabled());
        
        EmailTemplate updatedTemplate = emailTemplateRepository.save(existingTemplate);
        return modelMapper.map(updatedTemplate, EmailTemplateDto.class);
    }
    
    @Override
    public EmailTemplateDto getTemplate(Long id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with id: " + id));
        return modelMapper.map(template, EmailTemplateDto.class);
    }
    
    @Override
    public EmailTemplateDto getTemplateByCode(String code) {
        EmailTemplate template = emailTemplateRepository.findByCode(code)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with code: " + code));
        return modelMapper.map(template, EmailTemplateDto.class);
    }
    
    @Override
    public Page<EmailTemplateDto> getAllTemplates(Pageable pageable) {
        return emailTemplateRepository.findAll(pageable)
                .map(template -> modelMapper.map(template, EmailTemplateDto.class));
    }
    
    @Override
    public List<EmailTemplateDto> getTemplatesByCategory(TemplateCategory category) {
        return emailTemplateRepository.findByCategory(category).stream()
                .map(template -> modelMapper.map(template, EmailTemplateDto.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<EmailTemplateDto> getEnabledTemplates() {
        return emailTemplateRepository.findByEnabledTrue().stream()
                .map(template -> modelMapper.map(template, EmailTemplateDto.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<EmailTemplateDto> searchTemplates(String searchTerm, Pageable pageable) {
        return emailTemplateRepository.findByNameContainingOrDescriptionContaining(searchTerm, searchTerm, pageable)
                .map(template -> modelMapper.map(template, EmailTemplateDto.class));
    }
    
    @Override
    public void deleteTemplate(Long id) {
        if (!emailTemplateRepository.existsById(id)) {
            throw new TemplateNotFoundException("Template not found with id: " + id);
        }
        emailTemplateRepository.deleteById(id);
    }
    
    @Override
    public void toggleTemplateStatus(Long id, boolean enabled) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with id: " + id));
        template.setEnabled(enabled);
        emailTemplateRepository.save(template);
    }
    
    @Override
    public void setAsDefault(Long id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with id: " + id));
        
        // Set current template as default
        template.setDefault(true);
        
        // Unset other templates in the same category
        if (template.getCategory() != null) {
            emailTemplateRepository.findByCategoryAndIsDefaultTrue(template.getCategory())
                    .ifPresent(t -> {
                        t.setDefault(false);
                        emailTemplateRepository.save(t);
                    });
        }
        
        emailTemplateRepository.save(template);
    }
    
    @Override
    public String processTemplate(String templateCode, Map<String, Object> variables) {
        // Implementation for processing template with variables
        return null;
    }
    
    @Override
    public boolean validateTemplate(String content) {
        // Implementation for validating template syntax
        return false;
    }
    
    @Override
    public List<String> getTemplateVariables(String templateCode) {
        // Implementation for getting template variables
        return null;
    }
}