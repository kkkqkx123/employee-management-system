package com.example.demo.config.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Simple encryption converter for sensitive data.
 * In production, this should use proper encryption algorithms.
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    private static final String SECRET_KEY = "MySecretKey123"; // In production, use proper key management
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        // Simple Base64 encoding for demo purposes
        // In production, use proper encryption like AES
        return Base64.getEncoder().encodeToString((attribute + SECRET_KEY).getBytes());
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            // Simple Base64 decoding for demo purposes
            String decoded = new String(Base64.getDecoder().decode(dbData));
            return decoded.substring(0, decoded.length() - SECRET_KEY.length());
        } catch (Exception e) {
            return dbData; // Return as-is if decoding fails
        }
    }
}