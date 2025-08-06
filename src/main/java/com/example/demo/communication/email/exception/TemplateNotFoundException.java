package com.example.demo.communication.email.exception;

/**
 * Exception thrown when email template is not found
 */
public class TemplateNotFoundException extends RuntimeException {
    
    public TemplateNotFoundException(String templateCode) {
        super("Email template not found with code: " + templateCode);
    }
}