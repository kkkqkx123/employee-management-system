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