package com.example.demo.common.exception;

import lombok.Getter;

/**
 * Base exception for business logic errors.
 * 
 * Used for domain-specific errors that occur during business
 * operations and should be handled gracefully by the application.
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * Application-specific error code
     */
    private final String errorCode;
    
    /**
     * Additional context information
     */
    private final Object context;
    
    /**
     * Creates a business exception with message
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.context = null;
    }
    
    /**
     * Creates a business exception with error code and message
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.context = null;
    }
    
    /**
     * Creates a business exception with error code, message, and context
     */
    public BusinessException(String errorCode, String message, Object context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context;
    }
    
    /**
     * Creates a business exception with error code, message, and cause
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = null;
    }
    
    /**
     * Creates a business exception with all parameters
     */
    public BusinessException(String errorCode, String message, Object context, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = context;
    }
}