package com.example.demo.security.security;

import com.example.demo.security.entity.User;
import com.example.demo.security.entity.Role;
import com.example.demo.security.entity.Resource;
import com.example.demo.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userDetails", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        
        if (!user.getEnabled()) {
            log.warn("User account is disabled: {}", username);
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }
        
        Collection<GrantedAuthority> authorities = getAuthorities(user);
        
        log.debug("User {} loaded with {} authorities", username, authorities.size());
        
        return CustomUserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .passwordExpired(user.getPasswordExpired())
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .authorities(authorities)
                .build();
    }
    
    private Collection<GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Add role-based authorities
        for (Role role : user.getRoles()) {
            if (role.getActive()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                
                // Add resource-based authorities (permissions)
                for (Resource resource : role.getResources()) {
                    if (resource.getActive()) {
                        String permission = resource.getMethod() + ":" + resource.getUrl();
                        authorities.add(new SimpleGrantedAuthority(permission));
                    }
                }
            }
        }
        
        return authorities;
    }
    
    /**
     * Load user by ID for internal use
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userDetailsById", key = "#userId")
    public UserDetails loadUserById(Long userId) {
        log.debug("Loading user details for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });
        
        if (!user.getEnabled()) {
            log.warn("User account is disabled for ID: {}", userId);
            throw new UsernameNotFoundException("User account is disabled for ID: " + userId);
        }
        
        Collection<GrantedAuthority> authorities = getAuthorities(user);
        
        return CustomUserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .passwordExpired(user.getPasswordExpired())
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .authorities(authorities)
                .build();
    }
}