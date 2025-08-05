package com.example.demo.security.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserPrincipal implements UserDetails {
    
    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean accountNonLocked;
    private boolean passwordExpired;
    private boolean passwordChangeRequired;
    private Collection<? extends GrantedAuthority> authorities;
    
    // Default constructor (required for builder pattern)
    public CustomUserPrincipal() {
    }
    
    // Constructor with all fields
    public CustomUserPrincipal(Long id, String username, String password, String email, 
                              String firstName, String lastName, boolean enabled, 
                              boolean accountNonLocked, boolean passwordExpired, 
                              boolean passwordChangeRequired, 
                              Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.passwordExpired = passwordExpired;
        this.passwordChangeRequired = passwordChangeRequired;
        this.authorities = authorities;
    }
    
    // Getters and Setters (replacing Lombok @Data)
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
    
    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }
    
    public void setPasswordChangeRequired(boolean passwordChangeRequired) {
        this.passwordChangeRequired = passwordChangeRequired;
    }
    
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }
    
    // Builder method (in case Lombok fails)
    public static CustomUserPrincipalBuilder builder() {
        return new CustomUserPrincipalBuilder();
    }
    
    public static class CustomUserPrincipalBuilder {
        private Long id;
        private String username;
        private String password;
        private String email;
        private String firstName;
        private String lastName;
        private boolean enabled;
        private boolean accountNonLocked;
        private boolean passwordExpired;
        private boolean passwordChangeRequired;
        private Collection<? extends GrantedAuthority> authorities;
        
        public CustomUserPrincipalBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public CustomUserPrincipalBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public CustomUserPrincipalBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public CustomUserPrincipalBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public CustomUserPrincipalBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public CustomUserPrincipalBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public CustomUserPrincipalBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public CustomUserPrincipalBuilder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }
        
        public CustomUserPrincipalBuilder passwordExpired(boolean passwordExpired) {
            this.passwordExpired = passwordExpired;
            return this;
        }
        
        public CustomUserPrincipalBuilder passwordChangeRequired(boolean passwordChangeRequired) {
            this.passwordChangeRequired = passwordChangeRequired;
            return this;
        }
        
        public CustomUserPrincipalBuilder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }
        
        public CustomUserPrincipal build() {
            CustomUserPrincipal principal = new CustomUserPrincipal();
            principal.id = this.id;
            principal.username = this.username;
            principal.password = this.password;
            principal.email = this.email;
            principal.firstName = this.firstName;
            principal.lastName = this.lastName;
            principal.enabled = this.enabled;
            principal.accountNonLocked = this.accountNonLocked;
            principal.passwordExpired = this.passwordExpired;
            principal.passwordChangeRequired = this.passwordChangeRequired;
            principal.authorities = this.authorities;
            return principal;
        }
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true; // We don't track account expiration separately
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return !passwordExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}