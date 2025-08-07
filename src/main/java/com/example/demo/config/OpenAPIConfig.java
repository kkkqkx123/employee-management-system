package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 Configuration for Employee Management System
 * 
 * Configures Swagger UI documentation with JWT authentication,
 * API information, and proper security schemes.
 */
@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + contextPath)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.employee-management.com" + contextPath)
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", createAPIKeyScheme()))
                .tags(List.of(
                        new Tag().name("Authentication").description("User authentication and token management"),
                        new Tag().name("User Management").description("User CRUD operations and role management"),
                        new Tag().name("Role Management").description("Role and permission management"),
                        new Tag().name("Department Management").description("Department hierarchy and organization"),
                        new Tag().name("Employee Management").description("Employee information and lifecycle management"),
                        new Tag().name("Position Management").description("Job positions and titles management"),
                        new Tag().name("Communication").description("Email, chat, and notification systems"),
                        new Tag().name("Payroll Management").description("Payroll processing and financial management"),
                        new Tag().name("Import/Export").description("Data import and export operations"),
                        new Tag().name("System").description("System administration and monitoring")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Employee Management System API")
                .description("""
                        # Employee Management System REST API
                        
                        A comprehensive employee management system built with Spring Boot, featuring:
                        
                        ## Key Features
                        - **User Authentication & Authorization**: JWT-based security with role-based access control
                        - **Employee Management**: Complete employee lifecycle management with advanced search
                        - **Department Hierarchy**: Tree-structured department organization
                        - **Position Management**: Job titles and position classifications
                        - **Real-time Communication**: Chat system and notifications via WebSocket
                        - **Email System**: Template-based email communication with bulk sending
                        - **Payroll Processing**: Comprehensive payroll management and calculations
                        - **Import/Export**: Excel and CSV data import/export capabilities
                        - **Audit Trail**: Complete audit logging for all operations
                        
                        ## Architecture
                        - **Backend**: Spring Boot 3.5.4 with Java 24
                        - **Database**: PostgreSQL for persistent data, Redis for caching and real-time features
                        - **Security**: JWT tokens with role-based permissions
                        - **Real-time**: WebSocket for chat and notifications
                        - **Documentation**: OpenAPI 3.0 with Swagger UI
                        
                        ## Authentication
                        Most endpoints require authentication. Use the `/api/auth/login` endpoint to obtain a JWT token,
                        then include it in the `Authorization` header as `Bearer <token>`.
                        
                        ## Error Handling
                        All API responses follow a consistent format with success/error indicators and descriptive messages.
                        
                        ## Rate Limiting
                        API endpoints are rate-limited to prevent abuse. Check response headers for rate limit information.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Employee Management System Team")
                        .email("support@employee-management.com")
                        .url("https://github.com/employee-management/api"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        JWT Authorization header using the Bearer scheme.
                        
                        Enter 'Bearer' [space] and then your token in the text input below.
                        
                        Example: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                        """);
    }
}