package com.example.demo.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for StringUtil class.
 */
@SpringBootTest
class StringUtilTest {

    @Test
    void testIsEmpty() {
        // Empty strings
        assertThat(StringUtil.isEmpty(null)).isTrue();
        assertThat(StringUtil.isEmpty("")).isTrue();
        assertThat(StringUtil.isEmpty("   ")).isFalse(); // Spaces are not empty
        
        // Non-empty strings
        assertThat(StringUtil.isEmpty("test")).isFalse();
        assertThat(StringUtil.isEmpty(" test ")).isFalse();
    }

    @Test
    void testIsNotEmpty() {
        // Empty strings
        assertThat(StringUtil.isNotEmpty(null)).isFalse();
        assertThat(StringUtil.isNotEmpty("")).isFalse();
        
        // Non-empty strings
        assertThat(StringUtil.isNotEmpty("test")).isTrue();
        assertThat(StringUtil.isNotEmpty("   ")).isTrue(); // Spaces count as not empty
        assertThat(StringUtil.isNotEmpty(" test ")).isTrue();
    }

    @Test
    void testIsBlank() {
        // Blank strings
        assertThat(StringUtil.isBlank(null)).isTrue();
        assertThat(StringUtil.isBlank("")).isTrue();
        assertThat(StringUtil.isBlank("   ")).isTrue();
        assertThat(StringUtil.isBlank("\t\n\r")).isTrue();
        
        // Non-blank strings
        assertThat(StringUtil.isBlank("test")).isFalse();
        assertThat(StringUtil.isBlank(" test ")).isFalse();
    }

    @Test
    void testIsNotBlank() {
        // Blank strings
        assertThat(StringUtil.isNotBlank(null)).isFalse();
        assertThat(StringUtil.isNotBlank("")).isFalse();
        assertThat(StringUtil.isNotBlank("   ")).isFalse();
        
        // Non-blank strings
        assertThat(StringUtil.isNotBlank("test")).isTrue();
        assertThat(StringUtil.isNotBlank(" test ")).isTrue();
    }

    @Test
    void testTrim() {
        // Null handling
        assertThat(StringUtil.trim(null)).isNull();
        
        // Trimming
        assertThat(StringUtil.trim("")).isEqualTo("");
        assertThat(StringUtil.trim("test")).isEqualTo("test");
        assertThat(StringUtil.trim("  test  ")).isEqualTo("test");
        assertThat(StringUtil.trim("\t\ntest\r\n")).isEqualTo("test");
    }

    @Test
    void testTrimToEmpty() {
        // Null handling
        assertThat(StringUtil.trimToEmpty(null)).isEqualTo("");
        
        // Trimming
        assertThat(StringUtil.trimToEmpty("")).isEqualTo("");
        assertThat(StringUtil.trimToEmpty("test")).isEqualTo("test");
        assertThat(StringUtil.trimToEmpty("  test  ")).isEqualTo("test");
        assertThat(StringUtil.trimToEmpty("   ")).isEqualTo("");
    }

    @Test
    void testTrimToNull() {
        // Null handling
        assertThat(StringUtil.trimToNull(null)).isNull();
        
        // Trimming
        assertThat(StringUtil.trimToNull("")).isNull();
        assertThat(StringUtil.trimToNull("   ")).isNull();
        assertThat(StringUtil.trimToNull("test")).isEqualTo("test");
        assertThat(StringUtil.trimToNull("  test  ")).isEqualTo("test");
    }

    @Test
    void testDefaultIfEmpty() {
        String defaultValue = "default";
        
        // Empty strings
        assertThat(StringUtil.defaultIfEmpty(null, defaultValue)).isEqualTo(defaultValue);
        assertThat(StringUtil.defaultIfEmpty("", defaultValue)).isEqualTo(defaultValue);
        
        // Non-empty strings
        assertThat(StringUtil.defaultIfEmpty("test", defaultValue)).isEqualTo("test");
        assertThat(StringUtil.defaultIfEmpty("   ", defaultValue)).isEqualTo("   "); // Spaces are not empty
    }

    @Test
    void testDefaultIfBlank() {
        String defaultValue = "default";
        
        // Blank strings
        assertThat(StringUtil.defaultIfBlank(null, defaultValue)).isEqualTo(defaultValue);
        assertThat(StringUtil.defaultIfBlank("", defaultValue)).isEqualTo(defaultValue);
        assertThat(StringUtil.defaultIfBlank("   ", defaultValue)).isEqualTo(defaultValue);
        
        // Non-blank strings
        assertThat(StringUtil.defaultIfBlank("test", defaultValue)).isEqualTo("test");
        assertThat(StringUtil.defaultIfBlank(" test ", defaultValue)).isEqualTo(" test ");
    }

    @Test
    void testCapitalize() {
        // Null handling
        assertThat(StringUtil.capitalize(null)).isNull();
        
        // Capitalization
        assertThat(StringUtil.capitalize("")).isEqualTo("");
        assertThat(StringUtil.capitalize("test")).isEqualTo("Test");
        assertThat(StringUtil.capitalize("TEST")).isEqualTo("TEST");
        assertThat(StringUtil.capitalize("tEST")).isEqualTo("TEST");
        assertThat(StringUtil.capitalize("hello world")).isEqualTo("Hello world");
    }

    @Test
    void testUncapitalize() {
        // Null handling
        assertThat(StringUtil.uncapitalize(null)).isNull();
        
        // Uncapitalization
        assertThat(StringUtil.uncapitalize("")).isEqualTo("");
        assertThat(StringUtil.uncapitalize("Test")).isEqualTo("test");
        assertThat(StringUtil.uncapitalize("test")).isEqualTo("test");
        assertThat(StringUtil.uncapitalize("TEST")).isEqualTo("tEST");
        assertThat(StringUtil.uncapitalize("Hello World")).isEqualTo("hello World");
    }

    @Test
    void testToUpperCase() {
        // Null handling
        assertThat(StringUtil.toUpperCase(null)).isNull();
        
        // Upper case conversion
        assertThat(StringUtil.toUpperCase("")).isEqualTo("");
        assertThat(StringUtil.toUpperCase("test")).isEqualTo("TEST");
        assertThat(StringUtil.toUpperCase("Test")).isEqualTo("TEST");
        assertThat(StringUtil.toUpperCase("TEST")).isEqualTo("TEST");
        assertThat(StringUtil.toUpperCase("hello world")).isEqualTo("HELLO WORLD");
    }

    @Test
    void testToLowerCase() {
        // Null handling
        assertThat(StringUtil.toLowerCase(null)).isNull();
        
        // Lower case conversion
        assertThat(StringUtil.toLowerCase("")).isEqualTo("");
        assertThat(StringUtil.toLowerCase("TEST")).isEqualTo("test");
        assertThat(StringUtil.toLowerCase("Test")).isEqualTo("test");
        assertThat(StringUtil.toLowerCase("test")).isEqualTo("test");
        assertThat(StringUtil.toLowerCase("HELLO WORLD")).isEqualTo("hello world");
    }

    @Test
    void testReverse() {
        // Null handling
        assertThat(StringUtil.reverse(null)).isNull();
        
        // Reversal
        assertThat(StringUtil.reverse("")).isEqualTo("");
        assertThat(StringUtil.reverse("a")).isEqualTo("a");
        assertThat(StringUtil.reverse("test")).isEqualTo("tset");
        assertThat(StringUtil.reverse("hello world")).isEqualTo("dlrow olleh");
        assertThat(StringUtil.reverse("12345")).isEqualTo("54321");
    }

    @Test
    void testAbbreviate() {
        // Null handling
        assertThat(StringUtil.abbreviate(null, 10)).isNull();
        
        // No abbreviation needed
        assertThat(StringUtil.abbreviate("test", 10)).isEqualTo("test");
        assertThat(StringUtil.abbreviate("", 10)).isEqualTo("");
        
        // Abbreviation needed
        assertThat(StringUtil.abbreviate("hello world", 8)).isEqualTo("hello...");
        assertThat(StringUtil.abbreviate("testing", 5)).isEqualTo("te...");
        
        // Edge cases
        assertThat(StringUtil.abbreviate("test", 4)).isEqualTo("test"); // Exact length
        assertThat(StringUtil.abbreviate("test", 3)).isEqualTo("...");  // Too short for meaningful abbreviation
    }

    @Test
    void testRepeat() {
        // Null handling
        assertThat(StringUtil.repeat(null, 3)).isNull();
        
        // Zero repetitions
        assertThat(StringUtil.repeat("test", 0)).isEqualTo("");
        
        // Positive repetitions
        assertThat(StringUtil.repeat("a", 3)).isEqualTo("aaa");
        assertThat(StringUtil.repeat("ab", 2)).isEqualTo("abab");
        assertThat(StringUtil.repeat("test", 1)).isEqualTo("test");
        
        // Empty string
        assertThat(StringUtil.repeat("", 5)).isEqualTo("");
    }

    @Test
    void testJoin() {
        // Null handling
        assertThat(StringUtil.join(null, ",")).isEqualTo("");
        assertThat(StringUtil.join(List.of(), ",")).isEqualTo("");
        
        // Single element
        assertThat(StringUtil.join(List.of("test"), ",")).isEqualTo("test");
        
        // Multiple elements
        assertThat(StringUtil.join(List.of("a", "b", "c"), ",")).isEqualTo("a,b,c");
        assertThat(StringUtil.join(List.of("hello", "world"), " ")).isEqualTo("hello world");
        assertThat(StringUtil.join(List.of("1", "2", "3"), "-")).isEqualTo("1-2-3");
        
        // Null delimiter
        assertThat(StringUtil.join(List.of("a", "b"), null)).isEqualTo("ab");
        
        // Elements with null values
        assertThat(StringUtil.join(List.of("a", null, "c"), ",")).isEqualTo("a,,c");
    }

    @Test
    void testSplit() {
        // Null handling
        assertThat(StringUtil.split(null, ",")).isEmpty();
        assertThat(StringUtil.split("", ",")).isEmpty();
        
        // Single element
        assertThat(StringUtil.split("test", ",")).containsExactly("test");
        
        // Multiple elements
        assertThat(StringUtil.split("a,b,c", ",")).containsExactly("a", "b", "c");
        assertThat(StringUtil.split("hello world", " ")).containsExactly("hello", "world");
        assertThat(StringUtil.split("1-2-3", "-")).containsExactly("1", "2", "3");
        
        // Empty elements
        assertThat(StringUtil.split("a,,c", ",")).containsExactly("a", "", "c");
        assertThat(StringUtil.split(",a,", ",")).containsExactly("", "a", "");
        
        // Delimiter not found
        assertThat(StringUtil.split("test", ",")).containsExactly("test");
    }

    @Test
    void testContains() {
        // Null handling
        assertThat(StringUtil.contains(null, "test")).isFalse();
        assertThat(StringUtil.contains("test", null)).isFalse();
        
        // Contains
        assertThat(StringUtil.contains("hello world", "world")).isTrue();
        assertThat(StringUtil.contains("hello world", "hello")).isTrue();
        assertThat(StringUtil.contains("hello world", "o w")).isTrue();
        assertThat(StringUtil.contains("test", "test")).isTrue();
        assertThat(StringUtil.contains("test", "")).isTrue(); // Empty string is contained in any string
        
        // Does not contain
        assertThat(StringUtil.contains("hello world", "xyz")).isFalse();
        assertThat(StringUtil.contains("", "test")).isFalse();
    }

    @Test
    void testStartsWith() {
        // Null handling
        assertThat(StringUtil.startsWith(null, "test")).isFalse();
        assertThat(StringUtil.startsWith("test", null)).isFalse();
        
        // Starts with
        assertThat(StringUtil.startsWith("hello world", "hello")).isTrue();
        assertThat(StringUtil.startsWith("test", "test")).isTrue();
        assertThat(StringUtil.startsWith("test", "")).isTrue(); // Empty string starts any string
        
        // Does not start with
        assertThat(StringUtil.startsWith("hello world", "world")).isFalse();
        assertThat(StringUtil.startsWith("test", "testing")).isFalse();
        assertThat(StringUtil.startsWith("", "test")).isFalse();
    }

    @Test
    void testEndsWith() {
        // Null handling
        assertThat(StringUtil.endsWith(null, "test")).isFalse();
        assertThat(StringUtil.endsWith("test", null)).isFalse();
        
        // Ends with
        assertThat(StringUtil.endsWith("hello world", "world")).isTrue();
        assertThat(StringUtil.endsWith("test", "test")).isTrue();
        assertThat(StringUtil.endsWith("test", "")).isTrue(); // Empty string ends any string
        
        // Does not end with
        assertThat(StringUtil.endsWith("hello world", "hello")).isFalse();
        assertThat(StringUtil.endsWith("test", "testing")).isFalse();
        assertThat(StringUtil.endsWith("", "test")).isFalse();
    }

    @Test
    void testRemoveWhitespace() {
        // Null handling
        assertThat(StringUtil.removeWhitespace(null)).isNull();
        
        // Remove whitespace
        assertThat(StringUtil.removeWhitespace("")).isEqualTo("");
        assertThat(StringUtil.removeWhitespace("test")).isEqualTo("test");
        assertThat(StringUtil.removeWhitespace("hello world")).isEqualTo("helloworld");
        assertThat(StringUtil.removeWhitespace("  test  ")).isEqualTo("test");
        assertThat(StringUtil.removeWhitespace("\t\ntest\r\n")).isEqualTo("test");
        assertThat(StringUtil.removeWhitespace("a b c d")).isEqualTo("abcd");
    }

    @Test
    void testSanitizeForXss() {
        // Null handling
        assertThat(StringUtil.sanitizeForXss(null)).isNull();
        
        // Safe strings
        assertThat(StringUtil.sanitizeForXss("")).isEqualTo("");
        assertThat(StringUtil.sanitizeForXss("hello world")).isEqualTo("hello world");
        assertThat(StringUtil.sanitizeForXss("test123")).isEqualTo("test123");
        
        // XSS attempts
        assertThat(StringUtil.sanitizeForXss("<script>alert('xss')</script>"))
                .isEqualTo("&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;");
        assertThat(StringUtil.sanitizeForXss("test<img src=x onerror=alert(1)>"))
                .isEqualTo("test&lt;img src=x onerror=alert(1)&gt;");
        assertThat(StringUtil.sanitizeForXss("test\"onclick=\"alert(1)\""))
                .isEqualTo("test&quot;onclick=&quot;alert(1)&quot;");
        assertThat(StringUtil.sanitizeForXss("test'onload='alert(1)'"))
                .isEqualTo("test&#x27;onload=&#x27;alert(1)&#x27;");
    }

    @Test
    void testMaskSensitiveData() {
        // Null handling
        assertThat(StringUtil.maskSensitiveData(null)).isNull();
        
        // Short strings (show first and last char)
        assertThat(StringUtil.maskSensitiveData("")).isEqualTo("");
        assertThat(StringUtil.maskSensitiveData("a")).isEqualTo("*");
        assertThat(StringUtil.maskSensitiveData("ab")).isEqualTo("a*");
        assertThat(StringUtil.maskSensitiveData("abc")).isEqualTo("a*c");
        assertThat(StringUtil.maskSensitiveData("abcd")).isEqualTo("a**d");
        
        // Longer strings
        assertThat(StringUtil.maskSensitiveData("password")).isEqualTo("p******d");
        assertThat(StringUtil.maskSensitiveData("1234567890")).isEqualTo("1********0");
        assertThat(StringUtil.maskSensitiveData("sensitive_data")).isEqualTo("s************a");
    }
}