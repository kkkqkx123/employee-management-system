package com.example.demo.security.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.security.dto.UserCreateRequest;
import com.example.demo.security.dto.UserDto;
import com.example.demo.security.dto.UserUpdateRequest;
import com.example.demo.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * User Management Controller
 * 
 * Handles user management operations including CRUD operations, role assignments,
 * and user permissions. All endpoints require authentication and appropriate permissions.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User CRUD operations and role management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;    

    /**
     * Get all users with pagination
     * 
     * @param pageable Pagination parameters
     * @return Paginated list of users
     */
    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Retrieve paginated list of all users. Requires USER_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Users retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching all users with pagination: {}", pageable);
        
        Page<UserDto> users = userService.getAllUsers(pageable);
        
        log.info("Retrieved {} users", users.getTotalElements());
        
        return ResponseEntity.ok(
            ApiResponse.success(users, "Users retrieved successfully")
        );
    }
    
    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return User details
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieve user details by ID. Requires USER_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        
        log.info("Fetching user with ID: {}", id);
        
        UserDto user = userService.getUserById(id);
        
        log.info("Retrieved user: {}", user.getUsername());
        
        return ResponseEntity.ok(
            ApiResponse.success(user, "User retrieved successfully")
        );
    }    
    
/**
     * Search users by criteria
     * 
     * @param searchTerm Search term for username, first name, or last name
     * @param pageable Pagination parameters
     * @return Paginated list of matching users
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search users",
        description = "Search users by username, first name, or last name. Requires USER_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Search completed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Page<UserDto>>> searchUsers(
            @Parameter(description = "Search term", required = true)
            @RequestParam String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Searching users with term: '{}', pagination: {}", searchTerm, pageable);
        
        Page<UserDto> users = userService.searchUsers(searchTerm, pageable);
        
        log.info("Found {} users matching search term", users.getTotalElements());
        
        return ResponseEntity.ok(
            ApiResponse.success(users, "Search completed successfully")
        );
    }
    
    /**
     * Create new user
     * 
     * @param createRequest User creation data
     * @return Created user details
     */
    @PostMapping
    @Operation(
        summary = "Create new user",
        description = "Create a new user account. Requires USER_CREATE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "User created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid user data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Username or email already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody UserCreateRequest createRequest) {
        
        log.info("Creating new user with username: {}", createRequest.getUsername());
        
        UserDto createdUser = userService.createUser(createRequest);
        
        log.info("User created successfully with ID: {}", createdUser.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(createdUser, "User created successfully")
        );
    }    

    /**
     * Update existing user
     * 
     * @param id User ID
     * @param updateRequest Updated user data
     * @return Updated user details
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update user",
        description = "Update existing user details. Requires USER_UPDATE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid user data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        
        log.info("Updating user with ID: {}", id);
        
        UserDto updatedUser = userService.updateUser(id, updateRequest);
        
        log.info("User updated successfully: {}", updatedUser.getUsername());
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedUser, "User updated successfully")
        );
    }
    
    /**
     * Delete user
     * 
     * @param id User ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete user",
        description = "Delete user account. Requires USER_DELETE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        
        log.info("Deleting user with ID: {}", id);
        
        userService.deleteUser(id);
        
        log.info("User deleted successfully with ID: {}", id);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "User deleted successfully")
        );
    }    

    /**
     * Enable or disable user
     * 
     * @param id User ID
     * @param enabled Enable/disable flag
     * @return Success message
     */
    @PatchMapping("/{id}/enabled")
    @Operation(
        summary = "Enable/disable user",
        description = "Enable or disable user account. Requires USER_UPDATE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User status updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> setUserEnabled(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Enable/disable flag", required = true)
            @RequestParam boolean enabled) {
        
        log.info("Setting user {} enabled status to: {}", id, enabled);
        
        userService.setUserEnabled(id, enabled);
        
        String action = enabled ? "enabled" : "disabled";
        log.info("User {} successfully {}", id, action);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "User " + action + " successfully")
        );
    }
    
    /**
     * Get user roles
     * 
     * @param id User ID
     * @return User roles
     */
    @GetMapping("/{id}/roles")
    @Operation(
        summary = "Get user roles",
        description = "Retrieve roles assigned to user. Requires USER_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User roles retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserDto>> getUserRoles(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        
        log.info("Fetching roles for user ID: {}", id);
        
        UserDto user = userService.getUserById(id);
        
        log.info("Retrieved {} roles for user: {}", 
                user.getRoles() != null ? user.getRoles().size() : 0, 
                user.getUsername());
        
        return ResponseEntity.ok(
            ApiResponse.success(user, "User roles retrieved successfully")
        );
    }    

    /**
     * Assign roles to user
     * 
     * @param id User ID
     * @param roleIds Set of role IDs to assign
     * @return Success message
     */
    @PostMapping("/{id}/roles")
    @Operation(
        summary = "Assign roles to user",
        description = "Assign one or more roles to user. Requires ROLE_ASSIGN permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Roles assigned successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User or role not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    public ResponseEntity<ApiResponse<Void>> assignRolesToUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Role IDs to assign", required = true)
            @RequestBody Set<Long> roleIds) {
        
        log.info("Assigning roles {} to user ID: {}", roleIds, id);
        
        userService.assignRolesToUser(id, roleIds);
        
        log.info("Roles assigned successfully to user ID: {}", id);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Roles assigned successfully")
        );
    }
    
    /**
     * Remove roles from user
     * 
     * @param id User ID
     * @param roleIds Set of role IDs to remove
     * @return Success message
     */
    @DeleteMapping("/{id}/roles")
    @Operation(
        summary = "Remove roles from user",
        description = "Remove one or more roles from user. Requires ROLE_ASSIGN permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Roles removed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    public ResponseEntity<ApiResponse<Void>> removeRolesFromUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Role IDs to remove", required = true)
            @RequestBody Set<Long> roleIds) {
        
        log.info("Removing roles {} from user ID: {}", roleIds, id);
        
        userService.removeRolesFromUser(id, roleIds);
        
        log.info("Roles removed successfully from user ID: {}", id);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Roles removed successfully")
        );
    }    

    /**
     * Get user permissions
     * 
     * @param id User ID
     * @return Set of user permissions
     */
    @GetMapping("/{id}/permissions")
    @Operation(
        summary = "Get user permissions",
        description = "Retrieve all permissions for user. Requires USER_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User permissions retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Set<String>>> getUserPermissions(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        
        log.info("Fetching permissions for user ID: {}", id);
        
        Set<String> permissions = userService.getUserPermissions(id);
        
        log.info("Retrieved {} permissions for user ID: {}", permissions.size(), id);
        
        return ResponseEntity.ok(
            ApiResponse.success(permissions, "User permissions retrieved successfully")
        );
    }
    
    /**
     * Change user password
     * 
     * @param id User ID
     * @param oldPassword Current password
     * @param newPassword New password
     * @return Success message
     */
    @PatchMapping("/{id}/password")
    @Operation(
        summary = "Change user password",
        description = "Change user password. Requires current password verification."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Password changed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid current password"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('USER_UPDATE') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Current password", required = true)
            @RequestParam String oldPassword,
            @Parameter(description = "New password", required = true)
            @RequestParam String newPassword) {
        
        log.info("Password change request for user ID: {}", id);
        
        userService.changePassword(id, oldPassword, newPassword);
        
        log.info("Password changed successfully for user ID: {}", id);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Password changed successfully")
        );
    }    
 
   /**
     * Reset user password (admin function)
     * 
     * @param id User ID
     * @param newPassword New password
     * @return Success message
     */
    @PatchMapping("/{id}/password/reset")
    @Operation(
        summary = "Reset user password",
        description = "Reset user password (admin function). Requires PASSWORD_RESET permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Password reset successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('PASSWORD_RESET')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "New password", required = true)
            @RequestParam String newPassword) {
        
        log.info("Password reset request for user ID: {}", id);
        
        userService.resetPassword(id, newPassword);
        
        log.info("Password reset successfully for user ID: {}", id);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Password reset successfully")
        );
    }
}