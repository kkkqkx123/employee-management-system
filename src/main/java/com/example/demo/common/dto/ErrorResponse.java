package com.example.demo.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error response structure for API errors.
 * 
 * Provides detailed error information including error codes,
 * validation errors, and debugging information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Application-specific error code
     */
    private String errorCode;
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Error message for end users
     */
    private String message;
    
    /**
     * Detailed error description for developers
     */
    private String details;
    
    /**
     * Request path where the error occurred
     */
    private String path;
    
    /**
     * Timestamp when the error occurred
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Validation errors for form submissions
     */
    private Map<String, String> validationErrors;
    
    /**
     * List of error messages for multiple errors
     */
    private List<String> errors;
    
    /**
     * Additional context information
     */
    private Map<String, Object> context;
    
    /**
     * Creates a simple error response
     */
    public static ErrorResponse of(String errorCode, int status, String message) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .status(status)
                .message(message)
                .build();
    }
    
    /**
     * Creates an error response with validation errors
     */
    public static ErrorResponse withValidationErrors(String errorCode, int status, 
                                                   String message, Map<String, String> validationErrors) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .status(status)
                .message(message)
                .validationErrors(validationErrors)
                .build();
    }
}