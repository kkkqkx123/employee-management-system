package com.example.demo.communication.exception;

public class ChatMessageNotFoundException extends RuntimeException {
    public ChatMessageNotFoundException(String message) {
        super(message);
    }
    
    public ChatMessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}