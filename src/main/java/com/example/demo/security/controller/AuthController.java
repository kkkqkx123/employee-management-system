package com.example.demo.security.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.security.dto.LoginRequest;
import com.example.demo.security.dto.LoginResponse;
import com.example.demo.security.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Handles user authentication operations including login, logout, and token refresh.
 * All endpoints in this controller are publicly accessible.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and token management")
public class AuthController {
    
    private final AuthenticationService authenticationService;
    
    /**
     * Authenticate user with username and password
     * 
     * @param loginRequest Login credentials containing username and password
     * @return JWT token and user information upon successful authentication
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with username and password. Returns JWT token upon success."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Login successful"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Invalid credentials"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "423", 
            description = "Account locked due to too many failed attempts"
        )
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        log.info("Login attempt for username: {}", loginRequest.getUsername());
        
        try {
            LoginResponse loginResponse = authenticationService.authenticate(loginRequest);
            
            log.info("Login successful for username: {}", loginRequest.getUsername());
            
            return ResponseEntity.ok(
                ApiResponse.success(loginResponse, "Login successful")
            );
            
        } catch (Exception e) {
            log.warn("Login failed for username: {} - {}", loginRequest.getUsername(), e.getMessage());
            throw e; // Let global exception handler deal with it
        }
    }
    
    /**
     * Logout user by invalidating JWT token
     * 
     * @param request HTTP request containing Authorization header with JWT token
     * @return Success message upon successful logout
     */
    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Invalidate JWT token and logout user. Token will be blacklisted."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Logout successful"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid or missing token"
        )
    })
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Missing or invalid Authorization header")
            );
        }
        
        String token = authHeader.substring(7);
        String username = authenticationService.getUsernameFromToken(token);
        
        log.info("Logout request for user: {}", username);
        
        try {
            authenticationService.logout(token);
            
            log.info("Logout successful for user: {}", username);
            
            return ResponseEntity.ok(
                ApiResponse.success(null, "Logout successful")
            );
            
        } catch (Exception e) {
            log.warn("Logout failed for user: {} - {}", username, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Refresh JWT token
     * 
     * @param refreshToken Refresh token from the original login response
     * @return New JWT token with extended expiration
     */
    @PostMapping("/refresh-token")
    @Operation(
        summary = "Refresh JWT token",
        description = "Generate new JWT token using refresh token. Extends user session."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Token refreshed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Invalid or expired refresh token"
        )
    })
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Parameter(description = "Refresh token", required = true)
            @RequestParam String refreshToken) {
        
        log.info("Token refresh request");
        
        try {
            LoginResponse loginResponse = authenticationService.refreshToken(refreshToken);
            
            log.info("Token refresh successful for user: {}", loginResponse.getUser().getUsername());
            
            return ResponseEntity.ok(
                ApiResponse.success(loginResponse, "Token refreshed successfully")
            );
            
        } catch (Exception e) {
            log.warn("Token refresh failed - {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Validate JWT token
     * 
     * @param request HTTP request containing Authorization header with JWT token
     * @return Token validation status
     */
    @GetMapping("/validate-token")
    @Operation(
        summary = "Validate JWT token",
        description = "Check if JWT token is valid and not expired"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Token validation result"
        )
    })
    public ResponseEntity<ApiResponse<Boolean>> validateToken(HttpServletRequest request) {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(
                ApiResponse.success(false, "Missing or invalid Authorization header")
            );
        }
        
        String token = authHeader.substring(7);
        
        try {
            boolean isValid = authenticationService.validateToken(token);
            String message = isValid ? "Token is valid" : "Token is invalid or expired";
            
            return ResponseEntity.ok(
                ApiResponse.success(isValid, message)
            );
            
        } catch (Exception e) {
            log.warn("Token validation error - {}", e.getMessage());
            return ResponseEntity.ok(
                ApiResponse.success(false, "Token validation failed")
            );
        }
    }
}