package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test for the Spring Boot application.
 * 
 * Verifies that the application context loads successfully
 * with all configurations and dependencies.
 */
@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context
        // can be loaded successfully with all configurations
    }
}