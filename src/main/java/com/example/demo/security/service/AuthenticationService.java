package com.example.demo.security.service;

import com.example.demo.security.dto.LoginRequest;
import com.example.demo.security.dto.LoginResponse;

public interface AuthenticationService {
    
    /**
     * Authenticate user with username and password
     * @param loginRequest Login credentials
     * @return Login response with JWT token and user details
     */
    LoginResponse authenticate(LoginRequest loginRequest);
    
    /**
     * Logout user by invalidating JWT token
     * @param token JWT token to invalidate
     */
    void logout(String token);
    
    /**
     * Refresh JWT token
     * @param refreshToken Refresh token
     * @return New login response with refreshed token
     */
    LoginResponse refreshToken(String refreshToken);
    
    /**
     * Validate JWT token
     * @param token JWT token to validate
     * @return true if token is valid
     */
    boolean validateToken(String token);
    
    /**
     * Get username from JWT token
     * @param token JWT token
     * @return Username
     */
    String getUsernameFromToken(String token);
}