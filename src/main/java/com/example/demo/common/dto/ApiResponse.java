package com.example.demo.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for all REST endpoints.
 * 
 * Provides consistent response format across the application with
 * success/error status, data payload, and metadata.
 * 
 * @param <T> Type of the response data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * Indicates if the request was successful
     */
    private boolean success;
    
    /**
     * Human-readable message describing the result
     */
    private String message;
    
    /**
     * The actual response data
     */
    private T data;
    
    /**
     * Error details if the request failed
     */
    private ErrorResponse error;
    
    /**
     * Timestamp when the response was generated
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Request path for debugging purposes
     */
    private String path;
    
    /**
     * Creates a successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .build();
    }
    
    /**
     * Creates a successful response with data and custom message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Creates an error response with message
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * Creates an error response with detailed error information
     */
    public static <T> ApiResponse<T> error(String message, ErrorResponse error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .build();
    }
}