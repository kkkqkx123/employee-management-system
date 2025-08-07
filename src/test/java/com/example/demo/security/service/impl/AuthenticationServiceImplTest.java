package com.example.demo.security.service.impl;

import com.example.demo.security.dto.LoginRequest;
import com.example.demo.security.dto.LoginResponse;
import com.example.demo.security.entity.User;
import com.example.demo.security.exception.AuthenticationException;
import com.example.demo.security.exception.UserNotFoundException;
import com.example.demo.security.repository.UserRepository;
import com.example.demo.security.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationServiceImpl using Mockito.
 * Tests authentication logic, validation, and security.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setLoginAttempts(0);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void authenticate_shouldReturnTokenWhenCredentialsValid() {
        String expectedToken = "jwt.token.here";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn(expectedToken);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        LoginResponse result = authenticationService.authenticate(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(expectedToken);
        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
        verify(userRepository).save(argThat(u -> u.getLastLogin() != null));
    }

    @Test
    void authenticate_shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    void authenticate_shouldThrowExceptionWhenPasswordInvalid() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");

        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
        verify(userRepository).save(argThat(u -> u.getLoginAttempts() == 1));
    }

    @Test
    void authenticate_shouldThrowExceptionWhenUserDisabled() {
        // Given
        user.setEnabled(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("User account is disabled");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    void authenticate_shouldThrowExceptionWhenUserLocked() {
        // Given
        user.setAccountLocked(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("User account is locked");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
    void authenticate_shouldLockAccountAfterMaxFailedAttempts() {
        // Given
        user.setLoginAttempts(4); // One more failure will lock the account
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");

        verify(userRepository).save(argThat(u ->
                u.getLoginAttempts() == 5 && u.getAccountLocked()));
    }

    @Test
    void authenticate_shouldResetFailedAttemptsOnSuccessfulLogin() {
        // Given
        user.setLoginAttempts(3);
        String expectedToken = "jwt.token.here";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn(expectedToken);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        LoginResponse result = authenticationService.authenticate(loginRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(argThat(u -> u.getLoginAttempts() == 0));
    }

    @Test
    void validateToken_shouldReturnTrueWhenTokenValid() {
        // Given
        String token = "valid.jwt.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);

        // When
        boolean result = authenticationService.validateToken(token);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseWhenTokenInvalid() {
        // Given
        String token = "invalid.jwt.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // When
        boolean result = authenticationService.validateToken(token);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getUsernameFromToken_shouldReturnUsernameWhenTokenValid() {
        // Given
        String token = "valid.jwt.token";
        String expectedUsername = "testuser";
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(expectedUsername);

        // When
        String result = authenticationService.getUsernameFromToken(token);

        // Then
        assertThat(result).isEqualTo(expectedUsername);
    }

    @Test
    void refreshToken_shouldReturnNewTokenWhenCurrentTokenValid() {
        // Given
        String currentToken = "current.jwt.token";
        String newToken = "new.jwt.token";
        String username = "testuser";

        when(jwtTokenProvider.validateToken(currentToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(currentToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn(newToken);

        // When
        LoginResponse result = authenticationService.refreshToken(currentToken);

        // Then
        assertThat(result.getToken()).isEqualTo(newToken);
    }

    @Test
    void refreshToken_shouldThrowExceptionWhenTokenInvalid() {
        // Given
        String invalidToken = "invalid.jwt.token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshToken(invalidToken))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid token");

        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
    }
}