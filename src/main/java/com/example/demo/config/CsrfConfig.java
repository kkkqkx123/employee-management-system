package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.List;

/**
 * CSRF Protection Configuration.
 * 
 * Configures Cross-Site Request Forgery protection for state-changing operations
 * while allowing stateless API endpoints to bypass CSRF protection.
 */
@Configuration
public class CsrfConfig {

    @Value("${app.csrf.enabled:true}")
    private boolean csrfEnabled;

    @Value("${app.csrf.cookie.name:XSRF-TOKEN}")
    private String csrfCookieName;

    @Value("${app.csrf.header.name:X-XSRF-TOKEN}")
    private String csrfHeaderName;

    @Value("${app.csrf.parameter.name:_csrf}")
    private String csrfParameterName;

    @Value("${app.csrf.cookie.http-only:false}")
    private boolean csrfCookieHttpOnly;

    @Value("${app.csrf.cookie.secure:false}")
    private boolean csrfCookieSecure;

    @Value("${app.csrf.cookie.same-site:Lax}")
    private String csrfCookieSameSite;

    /**
     * CSRF token repository configuration
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
        repository.setCookieName(csrfCookieName);
        repository.setHeaderName(csrfHeaderName);
        repository.setParameterName(csrfParameterName);
        repository.setCookiePath("/");
        repository.setCookieCustomizer(customizer -> customizer
                .httpOnly(csrfCookieHttpOnly)
                .secure(csrfCookieSecure)
                .maxAge(-1)
                .sameSite(csrfCookieSameSite));
        
        return repository;
    }

    /**
     * CSRF token request handler with XOR encoding for additional security
     */
    @Bean
    public CsrfTokenRequestHandler csrfTokenRequestHandler() {
        XorCsrfTokenRequestAttributeHandler requestHandler = new XorCsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        return requestHandler;
    }

    /**
     * Request matcher for endpoints that should be excluded from CSRF protection
     */
    @Bean
    public RequestMatcher csrfExclusionMatcher() {
        return new CsrfExclusionRequestMatcher();
    }

    /**
     * Custom request matcher that excludes specific endpoints from CSRF protection
     */
    private static class CsrfExclusionRequestMatcher implements RequestMatcher {
        
        private final List<RequestMatcher> excludedMatchers = Arrays.asList(
            // API endpoints (stateless, JWT-based)
            new AntPathRequestMatcher("/api/**"),
            
            // WebSocket endpoints
            new AntPathRequestMatcher("/ws/**"),
            
            // Actuator endpoints
            new AntPathRequestMatcher("/actuator/**"),
            
            // Authentication endpoints
            new AntPathRequestMatcher("/auth/**"),
            
            // Public endpoints
            new AntPathRequestMatcher("/public/**"),
            
            // Health check endpoints
            new AntPathRequestMatcher("/health/**"),
            
            // Static resources
            new AntPathRequestMatcher("/static/**"),
            new AntPathRequestMatcher("/css/**"),
            new AntPathRequestMatcher("/js/**"),
            new AntPathRequestMatcher("/images/**"),
            new AntPathRequestMatcher("/favicon.ico"),
            
            // H2 Console (development only)
            new AntPathRequestMatcher("/h2-console/**")
        );

        @Override
        public boolean matches(HttpServletRequest request) {
            // Return true if the request should be excluded from CSRF protection
            return excludedMatchers.stream().anyMatch(matcher -> matcher.matches(request));
        }
    }

    /**
     * CSRF configuration for different environments
     */
    @Bean
    public CsrfEnvironmentConfig csrfEnvironmentConfig() {
        return new CsrfEnvironmentConfig();
    }

    /**
     * Environment-specific CSRF configuration
     */
    public static class CsrfEnvironmentConfig {
        
        /**
         * Development CSRF configuration (more lenient)
         */
        public CsrfTokenRepository developmentCsrfTokenRepository() {
            CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
            repository.setCookieName("DEV-XSRF-TOKEN");
            repository.setHeaderName("X-XSRF-TOKEN");
            repository.setCookiePath("/");
            repository.setCookieCustomizer(customizer -> customizer
                    .httpOnly(false)
                    .secure(false)
                    .maxAge(3600)); // 1 hour for development
            
            return repository;
        }

        /**
         * Production CSRF configuration (more secure)
         */
        public CsrfTokenRepository productionCsrfTokenRepository() {
            CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
            repository.setCookieName("XSRF-TOKEN");
            repository.setHeaderName("X-XSRF-TOKEN");
            repository.setCookiePath("/");
            repository.setCookieCustomizer(customizer -> customizer
                    .httpOnly(false) // Must be false for JavaScript access
                    .secure(true) // HTTPS only in production
                    .maxAge(-1)); // Session cookie
            
            return repository;
        }

        /**
         * Request matcher for production (more restrictive)
         */
        public RequestMatcher productionCsrfExclusionMatcher() {
            return new RequestMatcher() {
                private final List<RequestMatcher> excludedMatchers = Arrays.asList(
                    // Only API endpoints in production
                    new AntPathRequestMatcher("/api/**"),
                    new AntPathRequestMatcher("/actuator/health"),
                    new AntPathRequestMatcher("/actuator/info")
                );

                @Override
                public boolean matches(HttpServletRequest request) {
                    return excludedMatchers.stream().anyMatch(matcher -> matcher.matches(request));
                }
            };
        }
    }

    /**
     * CSRF token utility methods
     */
    @Bean
    public CsrfTokenUtil csrfTokenUtil() {
        return new CsrfTokenUtil();
    }

    /**
     * Utility class for CSRF token operations
     */
    public static class CsrfTokenUtil {
        
        /**
         * Check if CSRF protection should be applied to the request
         */
        public boolean shouldApplyCsrfProtection(HttpServletRequest request) {
            // Skip CSRF for safe HTTP methods
            String method = request.getMethod();
            if ("GET".equals(method) || "HEAD".equals(method) || 
                "TRACE".equals(method) || "OPTIONS".equals(method)) {
                return false;
            }
            
            // Skip CSRF for API endpoints (assuming JWT-based authentication)
            String requestURI = request.getRequestURI();
            if (requestURI.startsWith("/api/")) {
                return false;
            }
            
            // Apply CSRF for state-changing operations on web endpoints
            return true;
        }

        /**
         * Get CSRF token from request
         */
        public String getCsrfTokenFromRequest(HttpServletRequest request) {
            // Try header first
            String token = request.getHeader("X-XSRF-TOKEN");
            if (token != null) {
                return token;
            }
            
            // Try parameter
            token = request.getParameter("_csrf");
            if (token != null) {
                return token;
            }
            
            // Try custom header
            return request.getHeader("X-CSRF-TOKEN");
        }

        /**
         * Validate CSRF token format
         */
        public boolean isValidCsrfTokenFormat(String token) {
            return token != null && 
                   token.length() >= 32 && 
                   token.matches("^[a-zA-Z0-9+/=\\-_]+$");
        }
    }
}