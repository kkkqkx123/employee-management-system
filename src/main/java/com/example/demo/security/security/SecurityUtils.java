package com.example.demo.security.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Slf4j
public class SecurityUtils {
    
    private SecurityUtils() {
        // Utility class
    }
    
    /**
     * Get current authenticated user
     * @return Optional CustomUserPrincipal
     */
    public static Optional<CustomUserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal) {
            return Optional.of((CustomUserPrincipal) authentication.getPrincipal());
        }
        
        return Optional.empty();
    }
    
    /**
     * Get current user ID
     * @return Optional user ID
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(CustomUserPrincipal::getId);
    }
    
    /**
     * Get current username
     * @return Optional username
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(CustomUserPrincipal::getUsername);
    }
    
    /**
     * Check if current user is authenticated
     * @return true if authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !(authentication.getPrincipal() instanceof String);
    }
    
    /**
     * Check if current user has specific role
     * @param role Role name (without ROLE_ prefix)
     * @return true if user has role
     */
    public static boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role)))
                .orElse(false);
    }
    
    /**
     * Check if current user has specific permission
     * @param permission Permission string (e.g., "GET:/api/users")
     * @return true if user has permission
     */
    public static boolean hasPermission(String permission) {
        return getCurrentUser()
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(permission)))
                .orElse(false);
    }
    
    /**
     * Check if current user has any of the specified roles
     * @param roles Role names (without ROLE_ prefix)
     * @return true if user has any of the roles
     */
    public static boolean hasAnyRole(String... roles) {
        Optional<CustomUserPrincipal> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            return false;
        }
        
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if current user has all of the specified roles
     * @param roles Role names (without ROLE_ prefix)
     * @return true if user has all roles
     */
    public static boolean hasAllRoles(String... roles) {
        Optional<CustomUserPrincipal> currentUser = getCurrentUser();
        if (currentUser.isEmpty()) {
            return false;
        }
        
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get current user's full name
     * @return Optional full name
     */
    public static Optional<String> getCurrentUserFullName() {
        return getCurrentUser().map(CustomUserPrincipal::getFullName);
    }
    
    /**
     * Get current user's email
     * @return Optional email
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(CustomUserPrincipal::getEmail);
    }
    
    /**
     * Check if current user is the owner of a resource
     * @param resourceOwnerId ID of the resource owner
     * @return true if current user is the owner
     */
    public static boolean isResourceOwner(Long resourceOwnerId) {
        return getCurrentUserId()
                .map(userId -> userId.equals(resourceOwnerId))
                .orElse(false);
    }
    
    /**
     * Check if current user can access resource (owner or has admin role)
     * @param resourceOwnerId ID of the resource owner
     * @return true if user can access resource
     */
    public static boolean canAccessResource(Long resourceOwnerId) {
        return isResourceOwner(resourceOwnerId) || hasRole("ADMIN");
    }
}