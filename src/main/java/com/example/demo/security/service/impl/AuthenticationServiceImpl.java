package com.example.demo.security.service.impl;

import com.example.demo.security.dto.LoginRequest;
import com.example.demo.security.dto.LoginResponse;
import com.example.demo.security.dto.UserDto;
import com.example.demo.security.entity.User;
import com.example.demo.security.exception.AuthenticationException;
import com.example.demo.security.repository.UserRepository;
import com.example.demo.security.security.JwtTokenProvider;
import com.example.demo.security.security.CustomUserPrincipal;
import com.example.demo.security.service.AuthenticationService;
import com.example.demo.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "jwt:refresh:";
    
    @Override
    @Transactional
    public LoginResponse authenticate(LoginRequest loginRequest) {
        log.debug("Attempting authentication for user: {}", loginRequest.getUsername());
        
        try {
            // Find user first to handle account locking
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new AuthenticationException("Invalid username or password"));
            
            // Check if account is locked
            if (!user.isAccountNonLocked()) {
                log.warn("Authentication failed - account locked: {}", loginRequest.getUsername());
                throw new AuthenticationException("Account is temporarily locked due to multiple failed login attempts");
            }
            
            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            // Reset login attempts on successful authentication
            user.resetLoginAttempts();
            userRepository.save(user);
            
            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);
            Long expirationTime = jwtTokenProvider.getExpirationTime();
            
            // Generate refresh token if remember me is enabled
            String refreshToken = null;
            if (loginRequest.isRememberMe()) {
                refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
                // Store refresh token in Redis
                redisTemplate.opsForValue().set(
                        REFRESH_TOKEN_PREFIX + user.getUsername(),
                        refreshToken,
                        Duration.ofDays(30)
                );
            }
            
            // Get user details and permissions
            UserDto userDto = userService.getUserByUsername(loginRequest.getUsername());
            Set<String> permissions = userService.getUserPermissions(user.getId());
            
            log.info("User {} authenticated successfully", loginRequest.getUsername());
            
            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(expirationTime)
                    .user(userDto)
                    .permissions(permissions)
                    .loginTime(Instant.now())
                    .build();
                    
        } catch (BadCredentialsException e) {
            // Handle failed authentication
            handleFailedAuthentication(loginRequest.getUsername());
            throw new AuthenticationException("Invalid username or password");
        } catch (DisabledException e) {
            log.warn("Authentication failed - account disabled: {}", loginRequest.getUsername());
            throw new AuthenticationException("Account is disabled");
        } catch (LockedException e) {
            log.warn("Authentication failed - account locked: {}", loginRequest.getUsername());
            throw new AuthenticationException("Account is locked");
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }
    
    private void handleFailedAuthentication(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementLoginAttempts();
            userRepository.save(user);
            log.warn("Failed login attempt {} for user: {}", user.getLoginAttempts(), username);
        });
    }
    
    @Override
    public void logout(String token) {
        log.debug("Logging out user with token");
        
        if (jwtTokenProvider.validateToken(token)) {
            // Add token to blacklist
            Long expirationTime = jwtTokenProvider.getExpirationTimeFromToken(token);
            long ttl = expirationTime - System.currentTimeMillis() / 1000;
            
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_KEY_PREFIX + token,
                        "blacklisted",
                        Duration.ofSeconds(ttl)
                );
            }
            
            // Remove refresh token if exists
            String username = jwtTokenProvider.getUsernameFromToken(token);
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
            
            log.info("User {} logged out successfully", username);
        }
    }
    
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.debug("Attempting to refresh token");
        
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        // Check if refresh token exists in Redis
        String storedRefreshToken = (String) redisTemplate.opsForValue()
                .get(REFRESH_TOKEN_PREFIX + username);
        
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new AuthenticationException("Refresh token not found or expired");
        }
        
        // Generate new access token
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));
        
        // Load user details to create proper authentication
        CustomUserPrincipal userPrincipal = CustomUserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .passwordExpired(user.getPasswordExpired())
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .authorities(java.util.Collections.emptyList()) // Will be loaded by UserDetailsService
                .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        
        String newToken = jwtTokenProvider.generateToken(authentication);
        Long expirationTime = jwtTokenProvider.getExpirationTime();
        
        UserDto userDto = userService.getUserByUsername(username);
        Set<String> permissions = userService.getUserPermissions(user.getId());
        
        log.info("Token refreshed successfully for user: {}", username);
        
        return LoginResponse.builder()
                .token(newToken)
                .tokenType("Bearer")
                .expiresIn(expirationTime)
                .user(userDto)
                .permissions(permissions)
                .loginTime(Instant.now())
                .build();
    }
    
    @Override
    public boolean validateToken(String token) {
        // Check if token is blacklisted
        if (redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + token)) {
            return false;
        }
        
        return jwtTokenProvider.validateToken(token);
    }
    
    @Override
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }
}