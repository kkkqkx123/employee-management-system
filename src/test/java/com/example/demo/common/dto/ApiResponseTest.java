package com.example.demo.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ApiResponse class.
 */
@SpringBootTest
class ApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSuccessResponse() {
        // Given
        String data = "test data";
        
        // When
        ApiResponse<String> response = ApiResponse.success(data);
        
        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getMessage()).isEqualTo("Operation completed successfully");
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void testSuccessResponseWithCustomMessage() {
        // Given
        String data = "test data";
        String message = "Custom success message";
        
        // When
        ApiResponse<String> response = ApiResponse.success(data, message);
        
        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void testErrorResponse() {
        // Given
        String errorMessage = "Something went wrong";
        
        // When
        ApiResponse<Void> response = ApiResponse.error(errorMessage);
        
        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void testErrorResponseWithErrorDetails() {
        // Given
        String errorMessage = "Validation failed";
        ErrorResponse errorDetails = ErrorResponse.of("VALIDATION_ERROR", 400, "Field validation failed");
        
        // When
        ApiResponse<Void> response = ApiResponse.error(errorMessage, errorDetails);
        
        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getError()).isEqualTo(errorDetails);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void testJsonSerialization() throws Exception {
        // Given
        ApiResponse<String> response = ApiResponse.success("test data", "Success");
        response.setPath("/api/test");
        
        // When
        String json = objectMapper.writeValueAsString(response);
        
        // Then
        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("\"data\":\"test data\"");
        assertThat(json).contains("\"message\":\"Success\"");
        assertThat(json).contains("\"path\":\"/api/test\"");
        assertThat(json).contains("\"timestamp\":");
    }

    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"success\":true,\"message\":\"Test\",\"data\":\"test data\",\"timestamp\":\"2023-01-01T12:00:00\"}";
        
        // When
        ApiResponse<?> response = objectMapper.readValue(json, ApiResponse.class);
        
        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Test");
        assertThat(response.getData()).isEqualTo("test data");
    }

    @Test
    void testBuilderPattern() {
        // Given & When
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Builder test")
                .data("builder data")
                .path("/api/builder")
                .build();
        
        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Builder test");
        assertThat(response.getData()).isEqualTo("builder data");
        assertThat(response.getPath()).isEqualTo("/api/builder");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void testNullDataHandling() {
        // When
        ApiResponse<String> response = ApiResponse.success(null);
        
        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("Operation completed successfully");
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        ApiResponse<String> response1 = ApiResponse.success("data");
        ApiResponse<String> response2 = ApiResponse.success("data");
        
        // Set same timestamp to make them equal
        LocalDateTime timestamp = LocalDateTime.now();
        response1.setTimestamp(timestamp);
        response2.setTimestamp(timestamp);
        
        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        ApiResponse<String> response = ApiResponse.success("test");
        
        // When
        String toString = response.toString();
        
        // Then
        assertThat(toString).contains("ApiResponse");
        assertThat(toString).contains("success=true");
        assertThat(toString).contains("data=test");
    }
}