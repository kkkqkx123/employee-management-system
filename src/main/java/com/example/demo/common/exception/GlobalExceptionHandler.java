package com.example.demo.common.exception;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * 
 * Provides centralized exception handling with standardized
 * error responses and proper HTTP status codes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        log.warn("Business exception occurred: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .context(ex.getContext() != null ? Map.of("context", ex.getContext()) : null)
                .build();

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles validation exceptions with field errors
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        log.warn("Validation exception occurred: {} field errors", ex.getErrorCount());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .validationErrors(ex.getValidationErrors())
                .build();

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles Bean Validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        log.warn("Method argument validation failed: {} field errors", validationErrors.size());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed for request parameters")
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();

        ApiResponse<Object> response = ApiResponse.error("Validation failed", errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles bind exceptions from form data binding
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(
            BindException ex, WebRequest request) {
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        log.warn("Bind exception occurred: {} field errors", validationErrors.size());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("BINDING_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Data binding failed")
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();

        ApiResponse<Object> response = ApiResponse.error("Data binding failed", errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        Map<String, String> validationErrors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    ConstraintViolation::getMessage,
                    (existing, replacement) -> existing
                ));

        log.warn("Constraint violation occurred: {} violations", validationErrors.size());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("CONSTRAINT_VIOLATION")
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Constraint validation failed")
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();

        ApiResponse<Object> response = ApiResponse.error("Constraint validation failed", errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        log.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("AUTHENTICATION_FAILED")
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Authentication failed")
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .build();

        ApiResponse<Object> response = ApiResponse.error("Authentication failed", errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles bad credentials exceptions
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        log.warn("Bad credentials provided");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INVALID_CREDENTIALS")
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .build();

        ApiResponse<Object> response = ApiResponse.error("Invalid credentials", errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("ACCESS_DENIED")
                .status(HttpStatus.FORBIDDEN.value())
                .message("Access denied")
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .build();

        ApiResponse<Object> response = ApiResponse.error("Access denied", errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handles method argument type mismatch exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        log.warn("Method argument type mismatch: {} for parameter {}", 
                ex.getValue(), ex.getName());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INVALID_PARAMETER_TYPE")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(String.format("Invalid value '%s' for parameter '%s'", 
                        ex.getValue(), ex.getName()))
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .build();

        ApiResponse<Object> response = ApiResponse.error("Invalid parameter type", errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .path(getRequestPath(request))
                .timestamp(LocalDateTime.now())
                .build();

        ApiResponse<Object> response = ApiResponse.error("Internal server error", errorResponse);
        response.setPath(getRequestPath(request));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Extracts the request path from WebRequest
     */
    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}