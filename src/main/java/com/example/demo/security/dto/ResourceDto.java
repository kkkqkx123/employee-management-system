package com.example.demo.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

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
    
    private boolean active;
    
    private Instant createdAt;
    
    private Instant updatedAt;
}