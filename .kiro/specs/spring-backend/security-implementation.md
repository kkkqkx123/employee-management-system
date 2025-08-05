# Security and Permission Management Implementation

## Overview
This document provides detailed implementation specifications for the Security and Permission Management module of the Employee Management System. This module handles authentication, authorization, role-based access control, and permission management.

## Package Structure
```
com.example.demo.security/
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── RedisConfig.java
├── entity/
│   ├── User.java
│   ├── Role.java
│   └── Resource.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── ResourceRepository.java
├── service/
│   ├── UserService.java
│   ├── RoleService.java
│   ├── ResourceService.java
│   ├── AuthenticationService.java
│   └── PermissionService.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── RoleController.java
│   └── ResourceController.java
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── UserDto.java
│   ├── RoleDto.java
│   └── ResourceDto.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   ├── CustomUserDetailsService.java
│   └── SecurityUtils.java
└── exception/
    ├── AuthenticationException.java
    ├── AuthorizationException.java
    └── UserNotFoundException.java
```

## Entity Classes

### User Entity
```java
package com.example.demo.security.entity;

### User Entity
```java
package com.example.demo.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_enabled", columnList = "enabled"),
        @Index(name = "idx_user_account_locked", columnList = "account_locked")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    private String password;
    
    @Column(name = "email", nullable = false, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "last_login")
    private Instant lastLogin;
    
    @Column(name = "login_attempts", nullable = false)
    private Integer loginAttempts = 0;
    
    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;
    
    @Column(name = "account_locked_until")
    private Instant accountLockedUntil;
    
    @Column(name = "password_expired", nullable = false)
    private Boolean passwordExpired = false;
    
    @Column(name = "password_change_required", nullable = false)
    private Boolean passwordChangeRequired = false;
    
    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"),
        foreignKey = @ForeignKey(name = "fk_user_roles_user"),
        inverseForeignKey = @ForeignKey(name = "fk_user_roles_role")
    )
    private Set<Role> roles = new HashSet<>();
    
    public boolean isAccountNonLocked() {
        if (!accountLocked) return true;
        if (accountLockedUntil != null && Instant.now().isAfter(accountLockedUntil)) {
            accountLocked = false;
            accountLockedUntil = null;
            return true;
        }
        return false;
    }
    
    public void incrementLoginAttempts() {
        this.loginAttempts++;
        if (this.loginAttempts >= 5) {
            this.accountLocked = true;
            this.accountLockedUntil = Instant.now().plus(Duration.ofMinutes(30));
        }
    }
    
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountLocked = false;
        this.accountLockedUntil = null;
        this.lastLogin = Instant.now();
    }
}
```

### Role Entity
```java
package com.example.demo.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_name", columnList = "name"),
    @Index(name = "idx_role_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_resources",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    private Set<Resource> resources = new HashSet<>();
}
```

### Resource Entity
```java
package com.example.demo.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resources", indexes = {
    @Index(name = "idx_resource_url", columnList = "url"),
    @Index(name = "idx_resource_method", columnList = "method"),
    @Index(name = "idx_resource_category", columnList = "category"),
    @Index(name = "idx_resource_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "url", nullable = false, length = 255)
    private String url;
    
    @Column(name = "method", nullable = false, length = 10)
    private String method;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @ManyToMany(mappedBy = "resources", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
tory Interfaces

### UserRepository
```java
package com.example.demo.security.repository;

import com.example.demo.security.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByEnabledTrue();
    
    Page<User> findByEnabledTrue(Pageable pageable);
    
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
}
```

### RoleRepository
```java
package com.example.demo.security.repository;

import com.example.demo.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Role> findByEnabledTrue();
    
    List<Role> findByNameContainingIgnoreCase(String name);
}
```

### ResourceRepository
```java
package com.example.demo.security.repository;

import com.example.demo.security.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    Optional<Resource> findByNameAndMethod(String name, String method);
    
    List<Resource> findByEnabledTrue();
    
    List<Resource> findByCategory(String category);
    
    List<Resource> findByUrlContainingIgnoreCase(String url);
    
    boolean existsByNameAndMethod(String name, String method);
}
```
### LoginRequest
```java
package com.example.demo.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
    
    private boolean rememberMe = false;
}
```

### LoginResponse
```java
package com.example.demo.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String token;
    
    private String tokenType = "Bearer";
    
    private Long expiresIn; // seconds
    
    private UserDto user;
    
    private Set<String> permissions;
    
    private LocalDateTime loginTime;
}
```

### UserDto
```java
package com.example.demo.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginAt;
    
    private Set<RoleDto> roles;
}
```

### RoleDto
```java
package com.example.demo.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDto {
    
    private Long id;
    
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    private String name;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Set<ResourceDto> resources;
}
```

### ResourceDto
```java
package com.example.demo.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceDto {
    
    private Long id;
    
    @NotBlank(message = "Resource name is required")
    @Size(min = 2, max = 100, message = "Resource name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "URL is required")
    @Size(max = 255, message = "URL must not exceed 255 characters")
    private String url;
    
    @NotBlank(message = "HTTP method is required")
    @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH", message = "Method must be GET, POST, PUT, DELETE, or PATCH")
    private String method;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
```#
# Service Layer Implementation

### UserService
```java
package com.example.demo.security.service;

import com.example.demo.security.dto.UserDto;
import com.example.demo.security.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {
    
    /**
     * Create a new user
     * @param userDto User data transfer object
     * @return Created user DTO
     * @throws UserAlreadyExistsException if username or email already exists
     */
    UserDto createUser(UserDto userDto);
    
    /**
     * Update an existing user
     * @param id User ID
     * @param userDto Updated user data
     * @return Updated user DTO
     * @throws UserNotFoundException if user not found
     */
    UserDto updateUser(Long id, UserDto userDto);
    
    /**
     * Get user by ID
     * @param id User ID
     * @return User DTO
     * @throws UserNotFoundException if user not found
     */
    UserDto getUserById(Long id);
    
    /**
     * Get user by username
     * @param username Username
     * @return User DTO
     * @throws UserNotFoundException if user not found
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
     * @throws UserNotFoundException if user not found
     */
    void deleteUser(Long id);
    
    /**
     * Enable or disable user
     * @param id User ID
     * @param enabled Enable/disable flag
     * @throws UserNotFoundException if user not found
     */
    void setUserEnabled(Long id, boolean enabled);
    
    /**
     * Assign roles to user
     * @param userId User ID
     * @param roleIds Set of role IDs to assign
     * @throws UserNotFoundException if user not found
     * @throws RoleNotFoundException if any role not found
     */
    void assignRolesToUser(Long userId, Set<Long> roleIds);
    
    /**
     * Remove roles from user
     * @param userId User ID
     * @param roleIds Set of role IDs to remove
     * @throws UserNotFoundException if user not found
     */
    void removeRolesFromUser(Long userId, Set<Long> roleIds);
    
    /**
     * Get user permissions
     * @param userId User ID
     * @return Set of permission strings
     * @throws UserNotFoundException if user not found
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
     * @throws InvalidPasswordException if old password is incorrect
     * @throws UserNotFoundException if user not found
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * Reset user password (admin function)
     * @param userId User ID
     * @param newPassword New password
     * @throws UserNotFoundException if user not found
     */
    void resetPassword(Long userId, String newPassword);
}
```

### RoleService
```java
package com.example.demo.security.service;

import com.example.demo.security.dto.RoleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface RoleService {
    
    /**
     * Create a new role
     * @param roleDto Role data transfer object
     * @return Created role DTO
     * @throws RoleAlreadyExistsException if role name already exists
     */
    RoleDto createRole(RoleDto roleDto);
    
    /**
     * Update an existing role
     * @param id Role ID
     * @param roleDto Updated role data
     * @return Updated role DTO
     * @throws RoleNotFoundException if role not found
     */
    RoleDto updateRole(Long id, RoleDto roleDto);
    
    /**
     * Get role by ID
     * @param id Role ID
     * @return Role DTO
     * @throws RoleNotFoundException if role not found
     */
    RoleDto getRoleById(Long id);
    
    /**
     * Get role by name
     * @param name Role name
     * @return Role DTO
     * @throws RoleNotFoundException if role not found
     */
    RoleDto getRoleByName(String name);
    
    /**
     * Get all roles
     * @return List of role DTOs
     */
    List<RoleDto> getAllRoles();
    
    /**
     * Get all roles with pagination
     * @param pageable Pagination parameters
     * @return Page of role DTOs
     */
    Page<RoleDto> getAllRoles(Pageable pageable);
    
    /**
     * Search roles by name
     * @param searchTerm Search term for role name
     * @return List of matching role DTOs
     */
    List<RoleDto> searchRoles(String searchTerm);
    
    /**
     * Delete role by ID
     * @param id Role ID
     * @throws RoleNotFoundException if role not found
     * @throws RoleInUseException if role is assigned to users
     */
    void deleteRole(Long id);
    
    /**
     * Enable or disable role
     * @param id Role ID
     * @param enabled Enable/disable flag
     * @throws RoleNotFoundException if role not found
     */
    void setRoleEnabled(Long id, boolean enabled);
    
    /**
     * Assign resources to role
     * @param roleId Role ID
     * @param resourceIds Set of resource IDs to assign
     * @throws RoleNotFoundException if role not found
     * @throws ResourceNotFoundException if any resource not found
     */
    void assignResourcesToRole(Long roleId, Set<Long> resourceIds);
    
    /**
     * Remove resources from role
     * @param roleId Role ID
     * @param resourceIds Set of resource IDs to remove
     * @throws RoleNotFoundException if role not found
     */
    void removeResourcesFromRole(Long roleId, Set<Long> resourceIds);
    
    /**
     * Get role permissions
     * @param roleId Role ID
     * @return Set of permission strings
     * @throws RoleNotFoundException if role not found
     */
    Set<String> getRolePermissions(Long roleId);
}
```