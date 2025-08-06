package com.example.demo.communication.email.exception;

/**
 * Exception thrown when email log is not found
 */
public class EmailLogNotFoundException extends RuntimeException {
    
    public EmailLogNotFoundException(String message) {
        super(message);
    }
    
    public EmailLogNotFoundException(Long emailLogId) {
        super("Email log not found with ID: " + emailLogId);
    }
}