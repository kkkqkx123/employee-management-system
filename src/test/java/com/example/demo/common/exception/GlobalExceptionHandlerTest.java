package com.example.demo.common.exception;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.common.dto.ErrorResponse;
import com.example.demo.department.exception.DepartmentNotFoundException;
import com.example.demo.employee.exception.EmployeeNotFoundException;
import com.example.demo.security.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for GlobalExceptionHandler.
 */
@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandlerTest.TestConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/business-exception")
        public void throwBusinessException() {
            throw new BusinessException("Business rule violated");
        }

        @GetMapping("/validation-exception")
        public void throwValidationException() {
            throw new ValidationException("Validation failed");
        }

        @GetMapping("/employee-not-found")
        public void throwEmployeeNotFoundException() {
            throw new EmployeeNotFoundException("Employee not found");
        }

        @GetMapping("/department-not-found")
        public void throwDepartmentNotFoundException() {
            throw new DepartmentNotFoundException("Department not found");
        }

        @GetMapping("/user-not-found")
        public void throwUserNotFoundException() {
            throw new UserNotFoundException("User not found");
        }

        @GetMapping("/access-denied")
        public void throwAccessDeniedException() {
            throw new AccessDeniedException("Access denied");
        }

        @GetMapping("/constraint-violation")
        public void throwConstraintViolationException() {
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            Path propertyPath = mock(Path.class);
            when(violation.getPropertyPath()).thenReturn(propertyPath);
            when(propertyPath.toString()).thenReturn("email");
            when(violation.getMessage()).thenReturn("Invalid email format");
            
            throw new ConstraintViolationException("Validation failed", Set.of(violation));
        }

        @GetMapping("/bind-exception")
        public void throwBindException() throws BindException {
            BindException bindException = new BindException("target", "objectName");
            bindException.addError(new FieldError("objectName", "name", "Name is required"));
            bindException.addError(new FieldError("objectName", "email", "Email is invalid"));
            throw bindException;
        }

        @GetMapping("/runtime-exception")
        public void throwRuntimeException() {
            throw new RuntimeException("Unexpected error occurred");
        }

        @GetMapping("/illegal-argument")
        public void throwIllegalArgumentException() {
            throw new IllegalArgumentException("Invalid argument provided");
        }
    }

    @Test
    @WithMockUser
    void testBusinessExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Business rule violated");
        assertThat(response.getError()).isNotNull();
    }

    @Test
    @WithMockUser
    void testValidationExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/validation-exception"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Validation failed");
    }

    @Test
    @WithMockUser
    void testEmployeeNotFoundExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/employee-not-found"))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Employee not found");
    }

    @Test
    @WithMockUser
    void testDepartmentNotFoundExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/department-not-found"))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Department not found");
    }

    @Test
    @WithMockUser
    void testUserNotFoundExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/user-not-found"))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("User not found");
    }

    @Test
    @WithMockUser
    void testAccessDeniedExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Access denied");
    }

    @Test
    @WithMockUser
    void testConstraintViolationExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Validation failed");
        assertThat(response.getError()).isNotNull();
    }

    @Test
    @WithMockUser
    void testBindExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/bind-exception"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Validation failed");
        assertThat(response.getError()).isNotNull();
    }

    @Test
    @WithMockUser
    void testRuntimeExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/runtime-exception"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    @WithMockUser
    void testIllegalArgumentExceptionHandling() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid argument provided");
    }

    @Test
    @WithMockUser
    void testResponseStructure() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        // Verify response structure
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isNotNull();
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getPath()).isNotNull();
        assertThat(response.getError()).isNotNull();
        
        ErrorResponse error = response.getError();
        assertThat(error.getErrorCode()).isNotNull();
        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getMessage()).isNotNull();
        assertThat(error.getTimestamp()).isNotNull();
    }

    @Test
    @WithMockUser
    void testContentTypeIsJson() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String contentType = result.getResponse().getContentType();
                    assertThat(contentType).contains(MediaType.APPLICATION_JSON_VALUE);
                });
    }
}