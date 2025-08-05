package com.example.demo.security.exception;

public class RoleNotFoundException extends RuntimeException {
    
    public RoleNotFoundException(String message) {
        super(message);
    }
    
    public RoleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RoleNotFoundException(Long roleId) {
        super("Role not found with ID: " + roleId);
    }
}