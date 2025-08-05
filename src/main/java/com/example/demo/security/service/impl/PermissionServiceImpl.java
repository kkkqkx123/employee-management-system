package com.example.demo.security.service.impl;

import com.example.demo.security.entity.User;
import com.example.demo.security.entity.Role;
import com.example.demo.security.entity.Resource;
import com.example.demo.security.exception.UserNotFoundException;
import com.example.demo.security.repository.UserRepository;
import com.example.demo.security.repository.ResourceRepository;
import com.example.demo.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {
    
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userResourcePermissions", key = "#userId + ':' + #url + ':' + #method")
    public boolean hasPermission(Long userId, String url, String method) {
        log.debug("Checking permission for user ID: {}, URL: {}, Method: {}", userId, url, method);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (!user.getEnabled()) {
            log.debug("User ID: {} is disabled", userId);
            return false;
        }
        
        // Check direct resource permission
        for (Role role : user.getRoles()) {
            if (role.getActive()) {
                for (Resource resource : role.getResources()) {
                    if (resource.getActive() && 
                        resource.getUrl().equals(url) && 
                        resource.getMethod().equals(method)) {
                        log.debug("User ID: {} has direct permission for {}:{}", userId, method, url);
                        return true;
                    }
                }
            }
        }
        
        log.debug("User ID: {} does not have permission for {}:{}", userId, method, url);
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userRoleCheck", key = "#userId + ':' + #roles.toString()")
    public boolean hasAnyRole(Long userId, Set<String> roles) {
        log.debug("Checking if user ID: {} has any of roles: {}", userId, roles);
        
        Set<String> userRoles = getUserRoles(userId);
        boolean hasAnyRole = userRoles.stream().anyMatch(roles::contains);
        
        log.debug("User ID: {} {} any of the required roles", userId, 
                hasAnyRole ? "has" : "does not have");
        
        return hasAnyRole;
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userAllRolesCheck", key = "#userId + ':' + #roles.toString()")
    public boolean hasAllRoles(Long userId, Set<String> roles) {
        log.debug("Checking if user ID: {} has all roles: {}", userId, roles);
        
        Set<String> userRoles = getUserRoles(userId);
        boolean hasAllRoles = userRoles.containsAll(roles);
        
        log.debug("User ID: {} {} all required roles", userId, 
                hasAllRoles ? "has" : "does not have");
        
        return hasAllRoles;
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allUserPermissions", key = "#userId")
    public Set<String> getUserPermissions(Long userId) {
        log.debug("Getting all permissions for user ID: {}", userId);
        
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
    @Cacheable(value = "allUserRoles", key = "#userId")
    public Set<String> getUserRoles(Long userId) {
        log.debug("Getting all roles for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        Set<String> roleNames = user.getRoles().stream()
                .filter(Role::getActive)
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        log.debug("Found {} roles for user ID: {}", roleNames.size(), userId);
        return roleNames;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermissionByPattern(Long userId, String urlPattern, String method) {
        log.debug("Checking pattern permission for user ID: {}, URL pattern: {}, Method: {}", 
                userId, urlPattern, method);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (!user.getEnabled()) {
            log.debug("User ID: {} is disabled", userId);
            return false;
        }
        
        // Get matching resources by pattern
        List<Resource> matchingResources = resourceRepository
                .findByUrlPatternAndMethod(urlPattern, method);
        
        // Check if user has access to any matching resource
        Set<String> userPermissions = getUserPermissions(userId);
        
        for (Resource resource : matchingResources) {
            String permission = resource.getMethod() + ":" + resource.getUrl();
            if (userPermissions.contains(permission)) {
                log.debug("User ID: {} has pattern permission for {}:{}", userId, method, urlPattern);
                return true;
            }
        }
        
        log.debug("User ID: {} does not have pattern permission for {}:{}", userId, method, urlPattern);
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Set<String> validatePermissions(Long userId, Set<String> permissions) {
        log.debug("Validating permissions {} for user ID: {}", permissions, userId);
        
        Set<String> userPermissions = getUserPermissions(userId);
        Set<String> validPermissions = permissions.stream()
                .filter(userPermissions::contains)
                .collect(Collectors.toSet());
        
        log.debug("User ID: {} has {} out of {} requested permissions", 
                userId, validPermissions.size(), permissions.size());
        
        return validPermissions;
    }
}