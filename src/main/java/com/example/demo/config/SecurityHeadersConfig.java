package com.example.demo.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Security Headers Configuration.
 * 
 * Configures comprehensive security headers to protect against
 * common web vulnerabilities and attacks.
 */
@Configuration
public class SecurityHeadersConfig {

    @Value("${app.security.content-security-policy:default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:}")
    private String contentSecurityPolicy;

    @Value("${app.security.frame-options:DENY}")
    private String frameOptions;

    @Value("${app.security.content-type-options:nosniff}")
    private String contentTypeOptions;

    @Value("${app.security.referrer-policy:strict-origin-when-cross-origin}")
    private String referrerPolicy;

    @Value("${app.security.permissions-policy:geolocation=(), microphone=(), camera=()}")
    private String permissionsPolicy;

    /**
     * Security headers filter
     */
    @Bean
    public SecurityHeadersFilter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }

    /**
     * Content Security Policy header writer
     */
    @Bean
    public HeaderWriter contentSecurityPolicyHeaderWriter() {
        return (request, response) -> {
            if (!response.containsHeader("Content-Security-Policy")) {
                response.setHeader("Content-Security-Policy", contentSecurityPolicy);
            }
        };
    }

    /**
     * X-Frame-Options header writer
     */
    @Bean
    public HeaderWriter frameOptionsHeaderWriter() {
        return (request, response) -> {
            if (!response.containsHeader("X-Frame-Options")) {
                response.setHeader("X-Frame-Options", frameOptions);
            }
        };
    }

    /**
     * X-Content-Type-Options header writer
     */
    @Bean
    public HeaderWriter contentTypeOptionsHeaderWriter() {
        return (request, response) -> {
            if (!response.containsHeader("X-Content-Type-Options")) {
                response.setHeader("X-Content-Type-Options", contentTypeOptions);
            }
        };
    }

    /**
     * Referrer Policy header writer
     */
    @Bean
    public HeaderWriter referrerPolicyHeaderWriter() {
        return new ReferrerPolicyHeaderWriter(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN);
    }

    /**
     * X-XSS-Protection header writer
     */
    @Bean
    public HeaderWriter xssProtectionHeaderWriter() {
        return new XXssProtectionHeaderWriter();
    }

    /**
     * Permissions Policy header writer
     */
    @Bean
    public HeaderWriter permissionsPolicyHeaderWriter() {
        return (request, response) -> {
            if (!response.containsHeader("Permissions-Policy")) {
                response.setHeader("Permissions-Policy", permissionsPolicy);
            }
        };
    }

    /**
     * Strict Transport Security header writer
     */
    @Bean
    public HeaderWriter strictTransportSecurityHeaderWriter() {
        return (request, response) -> {
            if (request.isSecure() && !response.containsHeader("Strict-Transport-Security")) {
                response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
            }
        };
    }

    /**
     * Custom security headers filter
     */
    public class SecurityHeadersFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            // Add security headers
            addSecurityHeaders(request, response);
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
        }

        private void addSecurityHeaders(HttpServletRequest request, HttpServletResponse response) {
            // Content Security Policy
            if (!response.containsHeader("Content-Security-Policy")) {
                response.setHeader("Content-Security-Policy", contentSecurityPolicy);
            }

            // X-Frame-Options
            if (!response.containsHeader("X-Frame-Options")) {
                response.setHeader("X-Frame-Options", frameOptions);
            }

            // X-Content-Type-Options
            if (!response.containsHeader("X-Content-Type-Options")) {
                response.setHeader("X-Content-Type-Options", contentTypeOptions);
            }

            // Referrer Policy
            if (!response.containsHeader("Referrer-Policy")) {
                response.setHeader("Referrer-Policy", referrerPolicy);
            }

            // Permissions Policy
            if (!response.containsHeader("Permissions-Policy")) {
                response.setHeader("Permissions-Policy", permissionsPolicy);
            }

            // X-XSS-Protection
            if (!response.containsHeader("X-XSS-Protection")) {
                response.setHeader("X-XSS-Protection", "1; mode=block");
            }

            // Strict Transport Security (only for HTTPS)
            if (request.isSecure() && !response.containsHeader("Strict-Transport-Security")) {
                response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
            }

            // Cache Control for sensitive endpoints
            String requestURI = request.getRequestURI();
            if (requestURI.contains("/api/auth/") || requestURI.contains("/api/users/") || 
                requestURI.contains("/api/admin/")) {
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
            }

            // Remove server information
            response.setHeader("Server", "");
            
            // Add custom application headers
            response.setHeader("X-Application-Name", "Employee Management System");
            response.setHeader("X-Application-Version", "1.0.0");
        }
    }
}