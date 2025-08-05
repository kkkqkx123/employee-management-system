package com.example.demo.common.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Utility class for common string operations.
 * 
 * Provides string validation, formatting, and manipulation
 * methods used throughout the application.
 */
@UtilityClass
public class StringUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[1-9]\\d{1,14}$"
    );
    
    /**
     * Checks if a string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Checks if a string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * Checks if a string is null, empty, or contains only whitespace
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Checks if a string is not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * Capitalizes the first letter of a string
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * Converts a string to camelCase
     */
    public static String toCamelCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (char c : str.toCharArray()) {
            if (c == ' ' || c == '_' || c == '-') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
    
    /**
     * Validates an email address format
     */
    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates a phone number format
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        // Remove common separators
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
    
    /**
     * Masks sensitive information (e.g., email, phone)
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "*".repeat(username.length()) + "@" + domain;
        }
        
        return username.charAt(0) + "*".repeat(username.length() - 2) + 
               username.charAt(username.length() - 1) + "@" + domain;
    }
    
    /**
     * Masks phone number
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() < 4) {
            return "*".repeat(phone.length());
        }
        
        return "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }
    
    /**
     * Joins a collection of strings with a delimiter
     */
    public static String join(Collection<String> strings, String delimiter) {
        if (strings == null || strings.isEmpty()) {
            return "";
        }
        return String.join(delimiter, strings);
    }
    
    /**
     * Truncates a string to a maximum length
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Generates a random alphanumeric string
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            result.append(chars.charAt(index));
        }
        
        return result.toString();
    }
}