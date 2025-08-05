package com.example.demo.security.service;

import com.example.demo.security.dto.UserDto;
import com.example.demo.security.dto.UserCreateRequest;
import com.example.demo.security.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface UserService {
    
    /**
     * Create a new user
     * @param createRequest User creation data
     * @return Created user DTO
     */
    UserDto createUser(UserCreateRequest createRequest);
    
    /**
     * Update an existing user
     * @param id User ID
     * @param updateRequest Updated user data
     * @return Updated user DTO
     */
    UserDto updateUser(Long id, UserUpdateRequest updateRequest);
    
    /**
     * Get user by ID
     * @param id User ID
     * @return User DTO
     */
    UserDto getUserById(Long id);
    
    /**
     * Get user by username
     * @param username Username
     * @return User DTO
     */
    UserDto getUserByUsername(String username);
    
    /**
     * Get all users with pagination
     * @param pageable Pagination parameters
     * @return Page of user DTOs
     */
    Page<UserDto> getAllUsers(Pageable pageable);
    
    /**
     * Search users by criteria
     * @param searchTerm Search term for username, first name, or last name
     * @param pageable Pagination parameters
     * @return Page of matching user DTOs
     */
    Page<UserDto> searchUsers(String searchTerm, Pageable pageable);
    
    /**
     * Delete user by ID
     * @param id User ID
     */
    void deleteUser(Long id);
    
    /**
     * Enable or disable user
     * @param id User ID
     * @param enabled Enable/disable flag
     */
    void setUserEnabled(Long id, boolean enabled);
    
    /**
     * Assign roles to user
     * @param userId User ID
     * @param roleIds Set of role IDs to assign
     */
    void assignRolesToUser(Long userId, Set<Long> roleIds);
    
    /**
     * Remove roles from user
     * @param userId User ID
     * @param roleIds Set of role IDs to remove
     */
    void removeRolesFromUser(Long userId, Set<Long> roleIds);
    
    /**
     * Get user permissions
     * @param userId User ID
     * @return Set of permission strings
     */
    Set<String> getUserPermissions(Long userId);
    
    /**
     * Check if user has specific permission
     * @param userId User ID
     * @param permission Permission string
     * @return true if user has permission
     */
    boolean hasPermission(Long userId, String permission);
    
    /**
     * Update user last login time
     * @param userId User ID
     */
    void updateLastLoginTime(Long userId);
    
    /**
     * Change user password
     * @param userId User ID
     * @param oldPassword Current password
     * @param newPassword New password
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * Reset user password (admin function)
     * @param userId User ID
     * @param newPassword New password
     */
    void resetPassword(Long userId, String newPassword);
}