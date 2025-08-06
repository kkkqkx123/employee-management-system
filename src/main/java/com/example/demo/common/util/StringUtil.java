package com.example.demo.common.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntPredicate;
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
        return str == null || str.isEmpty();
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

    /**
     * Trims whitespace from the beginning and end of a string
     */
    public static String trim(String str) {
        if (str == null) {
            return null;
        }
        return str.trim();
    }

    /**
     * Trims whitespace from the beginning and end of a string, returning an empty string if the input is null or blank
     */
    public static String trimToEmpty(String str) {
        if (isBlank(str)) {
            return "";
        }
        return str.trim();
    }

    /**
     * Trims whitespace from the beginning and end of a string, returning null if the input is null or blank
     */
    public static String trimToNull(String str) {
        if (isBlank(str)) {
            return null;
        }
        return str.trim();
    }

    /**
     * Returns the string, or the default string if the string is null or empty
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        if (isEmpty(str)) {
            return defaultStr;
        }
        return str;
    }

    /**
     * Returns the string, or the default string if the string is null, empty, or contains only whitespace
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        if (isBlank(str)) {
            return defaultStr;
        }
        return str;
    }

    /**
     * Converts the first character of a string to lower case
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Converts a string to upper case
     */
    public static String toUpperCase(String str) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase();
    }

    /**
     * Converts a string to lower case
     */
    public static String toLowerCase(String str) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase();
    }

    /**
     * Reverses a string
     */
    public static String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * Abbreviates a string to a maximum length, appending ellipsis if truncated
     */
    public static String abbreviate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        if (maxLength < 3) {
            return "...";
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Repeats a string a specified number of times
     */
    public static String repeat(String str, int repeat) {
        if (str == null) {
            return null;
        }
        if (repeat <= 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            result.append(str);
        }
        return result.toString();
    }

    /**
     * Splits a string by a specified delimiter into a list of strings
     */
    public static List<String> split(String str, String delimiter) {
        if (isBlank(str)) {
            return new ArrayList<>();
        }
        if (delimiter == null) {
            return List.of(str);
        }
        return List.of(str.split(delimiter));
    }

    /**
     * Checks if a string contains a specified substring
     */
    public static boolean contains(String str, String searchStr) {
        if (isEmpty(str) || isEmpty(searchStr)) {
            return false;
        }
        return str.contains(searchStr);
    }

    /**
     * Checks if a string starts with a specified prefix
     */
    public static boolean startsWith(String str, String prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return false;
        }
        return str.startsWith(prefix);
    }

    /**
     * Checks if a string ends with a specified suffix
     */
    public static boolean endsWith(String str, String suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return false;
        }
        return str.endsWith(suffix);
    }

    /**
     * Removes all whitespace from a string
     */
    public static String removeWhitespace(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\\s+", "");
    }

    /**
     * Sanitizes a string for XSS protection
     */
    public static String sanitizeForXss(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("'", "&#x27;")
                   .replace("\"", "&quot;");
    }

    /**
     * Masks sensitive data in a string
     */
    public static String maskSensitiveData(String str) {
        if (isEmpty(str)) {
            return str;
        }
        if (str.length() <= 2) {
            return "*".repeat(str.length());
        }
        return str.charAt(0) + "*".repeat(str.length() - 2) + str.charAt(str.length() - 1);
    }
}
