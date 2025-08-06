package com.example.demo.common.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Exception for data validation errors.
 * 
 * Used when input data fails validation rules and contains
 * detailed information about validation failures.
 */
@Getter
public class ValidationException extends BusinessException {
    
    /**
     * Map of field names to validation error messages
     */
    private final Map<String, String> validationErrors;
    
    /**
     * Creates a validation exception with a general message
     */
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.validationErrors = null;
    }
    
    /**
     * Creates a validation exception with message and cause
     */
    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
        this.validationErrors = null;
    }
    
    /**
     * Creates a validation exception with field-specific errors
     */
    public ValidationException(String message, Map<String, String> validationErrors) {
        super("VALIDATION_ERROR", message, validationErrors);
        this.validationErrors = validationErrors;
    }
    
    /**
     * Creates a validation exception with error code and field errors
     */
    public ValidationException(String errorCode, String message, Map<String, String> validationErrors) {
        super(errorCode, message, validationErrors);
        this.validationErrors = validationErrors;
    }
    
    /**
     * Checks if this exception has field-specific validation errors
     */
    public boolean hasFieldErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }
    
    /**
     * Gets the number of validation errors
     */
    public int getErrorCount() {
        return validationErrors != null ? validationErrors.size() : 0;
    }
}