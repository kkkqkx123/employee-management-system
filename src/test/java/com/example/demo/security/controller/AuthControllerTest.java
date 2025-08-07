package com.example.demo.security.controller;

import com.example.demo.security.dto.LoginRequest;
import com.example.demo.security.dto.LoginResponse;
import com.example.demo.security.dto.UserDto;
import com.example.demo.security.service.AuthenticationService;
import com.example.demo.security.exception.AuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 * Tests authentication endpoints with MockMvc
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    private LoginRequest validLoginRequest;
    private LoginResponse loginResponse;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest("testuser", "password123", false);
        
        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .roles(Set.of())
                .build();

        loginResponse = LoginResponse.builder()
                .token("jwt-token-here")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(userDto)
                .permissions(Set.of("USER_READ"))
                .loginTime(Instant.now())
                .build();
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() throws Exception {
        // Given
        when(authenticationService.authenticate(any(LoginRequest.class)))
                .thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));

        verify(authenticationService).authenticate(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authenticationService.authenticate(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationService).authenticate(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidRequestBody_ShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest("", "", false);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).authenticate(any(LoginRequest.class));
    }

    @Test
    void login_WithMissingUsername_ShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest(null, "password123", false);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).authenticate(any(LoginRequest.class));
    }

    @Test
    void login_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest("testuser", "123", false);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).authenticate(any(LoginRequest.class));
    }

    @Test
    void logout_WithValidToken_ShouldReturnSuccess() throws Exception {
        // Given
        when(authenticationService.getUsernameFromToken(anyString()))
                .thenReturn("testuser");
        doNothing().when(authenticationService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer jwt-token-here"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(authenticationService).getUsernameFromToken("jwt-token-here");
        verify(authenticationService).logout("jwt-token-here");
    }

    @Test
    void logout_WithMissingAuthorizationHeader_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));

        verify(authenticationService, never()).logout(anyString());
    }

    @Test
    void logout_WithInvalidAuthorizationHeader_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Invalid-Header"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));

        verify(authenticationService, never()).logout(anyString());
    }

    @Test
    void refreshToken_WithValidRefreshToken_ShouldReturnNewToken() throws Exception {
        // Given
        when(authenticationService.refreshToken(anyString()))
                .thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .param("refreshToken", "valid-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-here"));

        verify(authenticationService).refreshToken("valid-refresh-token");
    }

    @Test
    void refreshToken_WithInvalidRefreshToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authenticationService.refreshToken(anyString()))
                .thenThrow(new AuthenticationException("Invalid refresh token"));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .param("refreshToken", "invalid-refresh-token"))
                .andExpect(status().isUnauthorized());

        verify(authenticationService).refreshToken("invalid-refresh-token");
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() throws Exception {
        // Given
        when(authenticationService.validateToken(anyString()))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/auth/validate-token")
                        .header("Authorization", "Bearer jwt-token-here"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(jsonPath("$.message").value("Token is valid"));

        verify(authenticationService).validateToken("jwt-token-here");
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() throws Exception {
        // Given
        when(authenticationService.validateToken(anyString()))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/auth/validate-token")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false))
                .andExpect(jsonPath("$.message").value("Token is invalid or expired"));

        verify(authenticationService).validateToken("invalid-token");
    }

    @Test
    void validateToken_WithMissingAuthorizationHeader_ShouldReturnFalse() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/validate-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false))
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));

        verify(authenticationService, never()).validateToken(anyString());
    }

    @Test
    void validateToken_WithException_ShouldReturnFalse() throws Exception {
        // Given
        when(authenticationService.validateToken(anyString()))
                .thenThrow(new RuntimeException("Token validation error"));

        // When & Then
        mockMvc.perform(get("/api/auth/validate-token")
                        .header("Authorization", "Bearer jwt-token-here"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false))
                .andExpect(jsonPath("$.message").value("Token validation failed"));

        verify(authenticationService).validateToken("jwt-token-here");
    }
}