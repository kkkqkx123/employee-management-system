package com.example.demo.communication.email.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for processing email templates with variable substitution
 */
@Component
@Slf4j
public class EmailTemplateProcessor {
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    /**
     * Process template content by replacing variables with actual values
     * @param templateContent Template content with variables in {{variable}} format
     * @param variables Map of variable names to values
     * @return Processed content with variables replaced
     */
    public String processTemplate(String templateContent, Map<String, Object> variables) {
        if (templateContent == null || templateContent.isEmpty()) {
            return templateContent;
        }
        
        if (variables == null || variables.isEmpty()) {
            log.warn("No variables provided for template processing");
            return templateContent;
        }
        
        String processedContent = templateContent;
        Matcher matcher = VARIABLE_PATTERN.matcher(templateContent);
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            String placeholder = matcher.group(0);
            
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "";
            
            processedContent = processedContent.replace(placeholder, replacement);
            
            if (value == null) {
                log.warn("Variable '{}' not found in provided variables map", variableName);
            }
        }
        
        return processedContent;
    }
    
    /**
     * Extract all variable names from template content
     * @param templateContent Template content
     * @return List of variable names found in the template
     */
    public List<String> extractVariables(String templateContent) {
        List<String> variables = new ArrayList<>();
        
        if (templateContent == null || templateContent.isEmpty()) {
            return variables;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(templateContent);
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            if (!variables.contains(variableName)) {
                variables.add(variableName);
            }
        }
        
        return variables;
    }
    
    /**
     * Validate template syntax
     * @param templateContent Template content to validate
     * @return true if template syntax is valid
     */
    public boolean validateTemplate(String templateContent) {
        if (templateContent == null) {
            return false;
        }
        
        try {
            // Check for balanced braces
            int openBraces = 0;
            for (int i = 0; i < templateContent.length() - 1; i++) {
                if (templateContent.charAt(i) == '{' && templateContent.charAt(i + 1) == '{') {
                    openBraces++;
                    i++; // Skip next character
                } else if (templateContent.charAt(i) == '}' && templateContent.charAt(i + 1) == '}') {
                    openBraces--;
                    i++; // Skip next character
                    if (openBraces < 0) {
                        return false; // More closing braces than opening
                    }
                }
            }
            
            return openBraces == 0; // All braces should be balanced
        } catch (Exception e) {
            log.error("Error validating template: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Preview template with sample data
     * @param templateContent Template content
     * @param sampleVariables Sample variable values
     * @return Preview of processed template
     */
    public String previewTemplate(String templateContent, Map<String, Object> sampleVariables) {
        try {
            return processTemplate(templateContent, sampleVariables);
        } catch (Exception e) {
            log.error("Error previewing template: {}", e.getMessage());
            return "Error processing template: " + e.getMessage();
        }
    }
}