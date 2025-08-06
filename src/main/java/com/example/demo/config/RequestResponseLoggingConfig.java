package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Request and Response Logging Configuration.
 * 
 * Provides comprehensive logging of HTTP requests and responses
 * for monitoring, debugging, and audit purposes.
 */
@Slf4j
@Configuration
public class RequestResponseLoggingConfig {

    @Value("${app.logging.request-response.enabled:true}")
    private boolean loggingEnabled;

    @Value("${app.logging.request-response.include-payload:false}")
    private boolean includePayload;

    @Value("${app.logging.request-response.max-payload-length:1000}")
    private int maxPayloadLength;

    @Value("${app.logging.request-response.excluded-paths:/actuator,/swagger-ui,/v3/api-docs}")
    private List<String> excludedPaths;

    /**
     * Request/Response logging filter
     */
    @Bean
    public RequestResponseLoggingFilter requestResponseLoggingFilter() {
        return new RequestResponseLoggingFilter();
    }

    /**
     * Custom logging filter implementation
     */
    public class RequestResponseLoggingFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                      @NonNull FilterChain filterChain) throws ServletException, IOException {
            
            if (!loggingEnabled || shouldSkipLogging(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Generate unique request ID
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            
            // Wrap request and response for content caching
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
            
            long startTime = System.currentTimeMillis();
            
            try {
                // Log request
                logRequest(wrappedRequest, requestId);
                
                // Process request
                filterChain.doFilter(wrappedRequest, wrappedResponse);
                
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                
                // Log response
                logResponse(wrappedResponse, requestId, duration);
                
                // Copy response content back to original response
                wrappedResponse.copyBodyToResponse();
            }
        }

        private boolean shouldSkipLogging(HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            return excludedPaths.stream().anyMatch(requestURI::startsWith);
        }

        private void logRequest(ContentCachingRequestWrapper request, String requestId) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("\n=== INCOMING REQUEST [").append(requestId).append("] ===\n");
            logMessage.append("Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
            logMessage.append("Method: ").append(request.getMethod()).append("\n");
            logMessage.append("URI: ").append(request.getRequestURI()).append("\n");
            
            if (request.getQueryString() != null) {
                logMessage.append("Query: ").append(request.getQueryString()).append("\n");
            }
            
            logMessage.append("Remote Address: ").append(getClientIpAddress(request)).append("\n");
            logMessage.append("User Agent: ").append(request.getHeader("User-Agent")).append("\n");
            
            // Log headers (excluding sensitive ones)
            logMessage.append("Headers:\n");
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                if (!isSensitiveHeader(headerName)) {
                    logMessage.append("  ").append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
                } else {
                    logMessage.append("  ").append(headerName).append(": [REDACTED]\n");
                }
            });
            
            // Log request body if enabled and not sensitive
            if (includePayload && !isSensitiveEndpoint(request.getRequestURI())) {
                String payload = getRequestPayload(request);
                if (payload != null && !payload.isEmpty()) {
                    logMessage.append("Payload: ").append(truncatePayload(payload)).append("\n");
                }
            }
            
            logMessage.append("=== END REQUEST [").append(requestId).append("] ===");
            
            log.info(logMessage.toString());
        }

        private void logResponse(ContentCachingResponseWrapper response, String requestId, long duration) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("\n=== OUTGOING RESPONSE [").append(requestId).append("] ===\n");
            logMessage.append("Status: ").append(response.getStatus()).append("\n");
            logMessage.append("Duration: ").append(duration).append("ms\n");
            
            // Log response headers
            logMessage.append("Headers:\n");
            response.getHeaderNames().forEach(headerName -> {
                logMessage.append("  ").append(headerName).append(": ").append(response.getHeader(headerName)).append("\n");
            });
            
            // Log response body if enabled
            if (includePayload) {
                String payload = getResponsePayload(response);
                if (payload != null && !payload.isEmpty()) {
                    logMessage.append("Payload: ").append(truncatePayload(payload)).append("\n");
                }
            }
            
            logMessage.append("=== END RESPONSE [").append(requestId).append("] ===");
            
            if (response.getStatus() >= 400) {
                log.error(logMessage.toString());
            } else {
                log.info(logMessage.toString());
            }
        }

        private String getClientIpAddress(HttpServletRequest request) {
            String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
            };

            for (String headerName : headerNames) {
                String ip = request.getHeader(headerName);
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    return ip.split(",")[0].trim();
                }
            }

            return request.getRemoteAddr();
        }

        private boolean isSensitiveHeader(String headerName) {
            List<String> sensitiveHeaders = Arrays.asList(
                "authorization", "cookie", "set-cookie", "x-auth-token", "x-api-key"
            );
            return sensitiveHeaders.contains(headerName.toLowerCase());
        }

        private boolean isSensitiveEndpoint(String uri) {
            List<String> sensitiveEndpoints = Arrays.asList(
                "/api/auth/login", "/api/auth/register", "/api/users/password"
            );
            return sensitiveEndpoints.stream().anyMatch(uri::contains);
        }

        private String getRequestPayload(ContentCachingRequestWrapper request) {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
            return null;
        }

        private String getResponsePayload(ContentCachingResponseWrapper response) {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
            return null;
        }

        private String truncatePayload(String payload) {
            if (payload.length() > maxPayloadLength) {
                return payload.substring(0, maxPayloadLength) + "... [TRUNCATED]";
            }
            return payload;
        }
    }
}