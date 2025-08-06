package com.example.demo.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ErrorResponse class.
 */
@SpringBootTest
class ErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSimpleErrorResponse() {
        // Given
        String errorCode = "VALIDATION_ERROR";
        int status = 400;
        String message = "Validation failed";
        
        // When
        ErrorResponse response = ErrorResponse.of(errorCode, status, message);
        
        // Then
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void testErrorResponseWithValidationErrors() {
        // Given
        String errorCode = "VALIDATION_ERROR";
        int status = 400;
        String message = "Field validation failed";
        Map<String, String> validationErrors = Map.of(
            "name", "Name is required",
            "email", "Invalid email format"
        );
        
        // When
        ErrorResponse response = ErrorResponse.withValidationErrors(errorCode, status, message, validationErrors);
        
        // Then
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getValidationErrors()).isEqualTo(validationErrors);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void testBuilderPattern() {
        // Given & When
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("BUSINESS_ERROR")
                .status(422)
                .message("Business rule violation")
                .details("Detailed error description")
                .path("/api/employees")
                .errors(List.of("Error 1", "Error 2"))
                .context(Map.of("userId", "123", "action", "create"))
                .build();
        
        // Then
        assertThat(response.getErrorCode()).isEqualTo("BUSINESS_ERROR");
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getMessage()).isEqualTo("Business rule violation");
        assertThat(response.getDetails()).isEqualTo("Detailed error description");
        assertThat(response.getPath()).isEqualTo("/api/employees");
        assertThat(response.getErrors()).containsExactly("Error 1", "Error 2");
        assertThat(response.getContext()).containsEntry("userId", "123");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void testJsonSerialization() throws Exception {
        // Given
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("TEST_ERROR")
                .status(500)
                .message("Test error")
                .details("Test details")
                .path("/api/test")
                .validationErrors(Map.of("field1", "error1"))
                .errors(List.of("General error"))
                .context(Map.of("key", "value"))
                .build();
        
        // When
        String json = objectMapper.writeValueAsString(response);
        
        // Then
        assertThat(json).contains("\"errorCode\":\"TEST_ERROR\"");
        assertThat(json).contains("\"status\":500");
        assertThat(json).contains("\"message\":\"Test error\"");
        assertThat(json).contains("\"details\":\"Test details\"");
        assertThat(json).contains("\"path\":\"/api/test\"");
        assertThat(json).contains("\"validationErrors\":{\"field1\":\"error1\"}");
        assertThat(json).contains("\"errors\":[\"General error\"]");
        assertThat(json).contains("\"context\":{\"key\":\"value\"}");
    }

    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = """
            {
                "errorCode": "DESERIALIZATION_TEST",
                "status": 400,
                "message": "Test message",
                "details": "Test details",
                "path": "/api/test",
                "timestamp": "2023-01-01T12:00:00",
                "validationErrors": {"field": "error"},
                "errors": ["error1", "error2"],
                "context": {"key": "value"}
            }
            """;
        
        // When
        ErrorResponse response = objectMapper.readValue(json, ErrorResponse.class);
        
        // Then
        assertThat(response.getErrorCode()).isEqualTo("DESERIALIZATION_TEST");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo("Test message");
        assertThat(response.getDetails()).isEqualTo("Test details");
        assertThat(response.getPath()).isEqualTo("/api/test");
        assertThat(response.getValidationErrors()).containsEntry("field", "error");
        assertThat(response.getErrors()).containsExactly("error1", "error2");
        assertThat(response.getContext()).containsEntry("key", "value");
    }

    @Test
    void testNullFieldsHandling() {
        // Given & When
        ErrorResponse response = ErrorResponse.of("ERROR", 400, "Message");
        
        // Then
        assertThat(response.getDetails()).isNull();
        assertThat(response.getPath()).isNull();
        assertThat(response.getValidationErrors()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getContext()).isNull();
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        
        ErrorResponse response1 = ErrorResponse.builder()
                .errorCode("TEST")
                .status(400)
                .message("Test")
                .timestamp(timestamp)
                .build();
                
        ErrorResponse response2 = ErrorResponse.builder()
                .errorCode("TEST")
                .status(400)
                .message("Test")
                .timestamp(timestamp)
                .build();
        
        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        ErrorResponse response = ErrorResponse.of("TEST_ERROR", 400, "Test message");
        
        // When
        String toString = response.toString();
        
        // Then
        assertThat(toString).contains("ErrorResponse");
        assertThat(toString).contains("errorCode=TEST_ERROR");
        assertThat(toString).contains("status=400");
        assertThat(toString).contains("message=Test message");
    }

    @Test
    void testJsonIncludeNonNull() throws Exception {
        // Given
        ErrorResponse response = ErrorResponse.of("SIMPLE_ERROR", 400, "Simple message");
        
        // When
        String json = objectMapper.writeValueAsString(response);
        
        // Then - null fields should not be included in JSON
        assertThat(json).doesNotContain("\"details\":");
        assertThat(json).doesNotContain("\"path\":");
        assertThat(json).doesNotContain("\"validationErrors\":");
        assertThat(json).doesNotContain("\"errors\":");
        assertThat(json).doesNotContain("\"context\":");
    }
}