package com.example.demo.security.service.impl;

import com.example.demo.security.dto.UserDto;
import com.example.demo.security.dto.UserCreateRequest;
import com.example.demo.security.dto.UserUpdateRequest;
import com.example.demo.security.dto.RoleDto;
import com.example.demo.security.entity.User;
import com.example.demo.security.entity.Role;
import com.example.demo.security.entity.Resource;
import com.example.demo.security.exception.UserNotFoundException;
import com.example.demo.security.exception.UserAlreadyExistsException;
import com.example.demo.security.exception.RoleNotFoundException;
import com.example.demo.security.exception.InvalidPasswordException;
import com.example.demo.security.repository.UserRepository;
import com.example.demo.security.repository.RoleRepository;
import com.example.demo.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest createRequest) {
        log.debug("Creating new user with username: {}", createRequest.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(createRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + createRequest.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(createRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + createRequest.getEmail());
        }
        
        User user = new User();
        user.setUsername(createRequest.getUsername());
        user.setEmail(createRequest.getEmail());
        user.setPassword(passwordEncoder.encode(createRequest.getPassword()));
        user.setFirstName(createRequest.getFirstName());
        user.setLastName(createRequest.getLastName());
        user.setEnabled(true);
        user.setPasswordChangedAt(Instant.now());
        
        // Assign roles if provided
        if (createRequest.getRoleIds() != null && !createRequest.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : createRequest.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RoleNotFoundException(roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return convertToDto(savedUser);
    }    
@Override
    @Transactional
    @CacheEvict(value = {"userDetails", "userDetailsById"}, key = "#id")
    public UserDto updateUser(Long id, UserUpdateRequest updateRequest) {
        log.debug("Updating user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(updateRequest.getEmail()) && 
            userRepository.existsByEmail(updateRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + updateRequest.getEmail());
        }
        
        user.setEmail(updateRequest.getEmail());
        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());
        
        if (updateRequest.getEnabled() != null) {
            user.setEnabled(updateRequest.getEnabled());
        }
        
        // Update roles if provided
        if (updateRequest.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : updateRequest.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RoleNotFoundException(roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }
        
        User savedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", savedUser.getId());
        
        return convertToDto(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        log.debug("Getting user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        return convertToDto(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersByUsername", key = "#username")
    public UserDto getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username, true));
        
        return convertToDto(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination: {}", pageable);
        
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {} and pagination: {}", searchTerm, pageable);
        
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(this::convertToDto);
    }  
  @Override
    @Transactional
    @CacheEvict(value = {"users", "userDetails", "userDetailsById"}, key = "#id")
    public void deleteUser(Long id) {
        log.debug("Deleting user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"users", "userDetails", "userDetailsById"}, key = "#id")
    public void setUserEnabled(Long id, boolean enabled) {
        log.debug("Setting user enabled status to {} for ID: {}", enabled, id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        user.setEnabled(enabled);
        userRepository.save(user);
        
        log.info("User enabled status updated to {} for ID: {}", enabled, id);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"users", "userDetails", "userDetailsById"}, key = "#userId")
    public void assignRolesToUser(Long userId, Set<Long> roleIds) {
        log.debug("Assigning roles {} to user ID: {}", roleIds, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        Set<Role> rolesToAdd = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RoleNotFoundException(roleId));
            rolesToAdd.add(role);
        }
        
        user.getRoles().addAll(rolesToAdd);
        userRepository.save(user);
        
        log.info("Roles assigned successfully to user ID: {}", userId);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"users", "userDetails", "userDetailsById"}, key = "#userId")
    public void removeRolesFromUser(Long userId, Set<Long> roleIds) {
        log.debug("Removing roles {} from user ID: {}", roleIds, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        Set<Role> rolesToRemove = user.getRoles().stream()
                .filter(role -> roleIds.contains(role.getId()))
                .collect(Collectors.toSet());
        
        user.getRoles().removeAll(rolesToRemove);
        userRepository.save(user);
        
        log.info("Roles removed successfully from user ID: {}", userId);
    }   
 @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId")
    public Set<String> getUserPermissions(Long userId) {
        log.debug("Getting permissions for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        Set<String> permissions = new HashSet<>();
        
        for (Role role : user.getRoles()) {
            if (role.getActive()) {
                for (Resource resource : role.getResources()) {
                    if (resource.getActive()) {
                        String permission = resource.getMethod() + ":" + resource.getUrl();
                        permissions.add(permission);
                    }
                }
            }
        }
        
        log.debug("Found {} permissions for user ID: {}", permissions.size(), userId);
        return permissions;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permission) {
        log.debug("Checking permission '{}' for user ID: {}", permission, userId);
        
        Set<String> userPermissions = getUserPermissions(userId);
        boolean hasPermission = userPermissions.contains(permission);
        
        log.debug("User ID: {} {} permission '{}'", userId, 
                hasPermission ? "has" : "does not have", permission);
        
        return hasPermission;
    }
    
    @Override
    @Transactional
    public void updateLastLoginTime(Long userId) {
        log.debug("Updating last login time for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setLastLogin(Instant.now());
        userRepository.save(user);
        
        log.debug("Last login time updated for user ID: {}", userId);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"userDetails", "userDetailsById"}, key = "#userId")
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.debug("Changing password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        user.setPasswordChangeRequired(false);
        userRepository.save(user);
        
        log.info("Password changed successfully for user ID: {}", userId);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"userDetails", "userDetailsById"}, key = "#userId")
    public void resetPassword(Long userId, String newPassword) {
        log.debug("Resetting password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        user.setPasswordChangeRequired(true); // Force password change on next login
        userRepository.save(user);
        
        log.info("Password reset successfully for user ID: {}", userId);
    }  
  private UserDto convertToDto(User user) {
        Set<RoleDto> roleDtos = user.getRoles().stream()
                .map(this::convertRoleToDto)
                .collect(Collectors.toSet());
        
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .roles(roleDtos)
                .build();
    }
    
    private RoleDto convertRoleToDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}