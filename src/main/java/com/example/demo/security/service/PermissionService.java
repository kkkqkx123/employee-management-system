package com.example.demo.security.service;

import java.util.Set;

public interface PermissionService {
    
    /**
     * Check if user has permission to access a resource
     * @param userId User ID
     * @param url Resource URL
     * @param method HTTP method
     * @return true if user has permission
     */
    boolean hasPermission(Long userId, String url, String method);
    
    /**
     * Check if user has any of the specified roles
     * @param userId User ID
     * @param roles Set of role names
     * @return true if user has any of the roles
     */
    boolean hasAnyRole(Long userId, Set<String> roles);
    
    /**
     * Check if user has all of the specified roles
     * @param userId User ID
     * @param roles Set of role names
     * @return true if user has all roles
     */
    boolean hasAllRoles(Long userId, Set<String> roles);
    
    /**
     * Get all permissions for a user
     * @param userId User ID
     * @return Set of permission strings in format "METHOD:URL"
     */
    Set<String> getUserPermissions(Long userId);
    
    /**
     * Get all role names for a user
     * @param userId User ID
     * @return Set of role names
     */
    Set<String> getUserRoles(Long userId);
    
    /**
     * Check if user can access resource based on URL pattern matching
     * @param userId User ID
     * @param urlPattern URL pattern to match
     * @param method HTTP method
     * @return true if user has permission
     */
    boolean hasPermissionByPattern(Long userId, String urlPattern, String method);
    
    /**
     * Validate user permissions for multiple resources
     * @param userId User ID
     * @param permissions Set of permissions to check
     * @return Set of permissions the user actually has
     */
    Set<String> validatePermissions(Long userId, Set<String> permissions);
}