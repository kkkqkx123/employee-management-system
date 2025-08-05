package com.example.demo.security.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Converter
@Component
@RequiredArgsConstructor
@Slf4j
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    private final AESUtil aesUtil;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        
        try {
            String encrypted = aesUtil.encrypt(attribute);
            log.debug("Successfully encrypted field for database storage");
            return encrypted;
        } catch (Exception e) {
            log.error("Error encrypting field for database storage", e);
            throw new RuntimeException("Failed to encrypt sensitive data", e);
        }
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        try {
            String decrypted = aesUtil.decrypt(dbData);
            log.debug("Successfully decrypted field from database");
            return decrypted;
        } catch (Exception e) {
            log.error("Error decrypting field from database", e);
            throw new RuntimeException("Failed to decrypt sensitive data", e);
        }
    }
}