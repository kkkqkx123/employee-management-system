package com.example.demo.security.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.Set;

@Data
public class UserUpdateRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(max = 50)
    private String firstName;
    
    @NotBlank
    @Size(max = 50)
    private String lastName;
    
    private Boolean enabled;
    
    private Set<Long> roleIds;
}