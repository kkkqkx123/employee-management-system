package com.example.demo.security.exception;

public class RoleInUseException extends RuntimeException {
    
    public RoleInUseException(String message) {
        super(message);
    }
    
    public RoleInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}