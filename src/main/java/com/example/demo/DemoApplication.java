package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class for Employee Management System.
 * 
 * This application provides comprehensive employee management functionality
 * with hybrid PostgreSQL + Redis architecture for optimal performance.
 * 
 * Features:
 * - Employee, Department, and Position management
 * - JWT-based authentication and authorization
 * - Real-time chat and notifications via WebSocket
 * - Email communication system
 * - Payroll management
 * - File import/export capabilities
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}