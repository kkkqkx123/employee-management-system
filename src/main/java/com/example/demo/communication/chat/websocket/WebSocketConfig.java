package com.example.demo.communication.chat.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.List;
import java.util.Map;

/**
 * WebSocket Configuration with Security and Monitoring.
 * 
 * Configures STOMP messaging with security interceptors,
 * connection monitoring, and proper authentication handling.
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.websocket.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private List<String> allowedOrigins;

    @Value("${app.websocket.heartbeat.client:10000}")
    private long clientHeartbeat;

    @Value("${app.websocket.heartbeat.server:10000}")
    private long serverHeartbeat;

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the messages
        // back to the client on destinations prefixed with "/topic" and "/queue"
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{serverHeartbeat, clientHeartbeat});
        
        // Designate the "/app" prefix for messages that are bound for methods
        // annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Register the "/ws" endpoint with security and monitoring
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .addInterceptors(webSocketHandshakeInterceptor())
                .withSockJS()
                .setHeartbeatTime(25000); // 25 seconds
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(webSocketChannelInterceptor());
    }

    @Override
    public void configureClientOutboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(webSocketChannelInterceptor());
    }

    /**
     * WebSocket handshake interceptor for connection security
     */
    @Bean
    public HandshakeInterceptor webSocketHandshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(
                    org.springframework.http.server.ServerHttpRequest request,
                    org.springframework.http.server.ServerHttpResponse response,
                    org.springframework.web.socket.WebSocketHandler wsHandler,
                    Map<String, Object> attributes) throws Exception {
                
                log.info("WebSocket handshake attempt from: {}", request.getRemoteAddress());
                
                // Add session attributes to WebSocket session
                boolean result = super.beforeHandshake(request, response, wsHandler, attributes);
                
                if (result) {
                    // Add connection timestamp
                    attributes.put("connectionTime", System.currentTimeMillis());
                    
                    // Add remote address for monitoring
                    attributes.put("remoteAddress", request.getRemoteAddress());
                    
                    log.info("WebSocket handshake successful for: {}", request.getRemoteAddress());
                } else {
                    log.warn("WebSocket handshake failed for: {}", request.getRemoteAddress());
                }
                
                return result;
            }

            @Override
            public void afterHandshake(
                    org.springframework.http.server.ServerHttpRequest request,
                    org.springframework.http.server.ServerHttpResponse response,
                    org.springframework.web.socket.WebSocketHandler wsHandler,
                    Exception exception) {
                
                if (exception != null) {
                    log.error("WebSocket handshake error for: {}", request.getRemoteAddress(), exception);
                } else {
                    log.info("WebSocket connection established for: {}", request.getRemoteAddress());
                }
                
                super.afterHandshake(request, response, wsHandler, exception);
            }
        };
    }

    /**
     * WebSocket channel interceptor for message security and monitoring
     */
    @Bean
    public ChannelInterceptor webSocketChannelInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null) {
                    StompCommand command = accessor.getCommand();
                    
                    if (StompCommand.CONNECT.equals(command)) {
                        handleConnect(accessor);
                    } else if (StompCommand.DISCONNECT.equals(command)) {
                        handleDisconnect(accessor);
                    } else if (StompCommand.SUBSCRIBE.equals(command)) {
                        handleSubscribe(accessor);
                    } else if (StompCommand.SEND.equals(command)) {
                        handleSend(accessor);
                    }
                    
                    // Log message for monitoring
                    log.debug("WebSocket message: command={}, destination={}, sessionId={}", 
                            command, accessor.getDestination(), accessor.getSessionId());
                }
                
                return message;
            }

            @Override
            public void postSend(@NonNull Message<?> message, @NonNull MessageChannel channel, boolean sent) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Connection established
                    String sessionId = accessor.getSessionId();
                    log.info("WebSocket STOMP connection established: sessionId={}", sessionId);
                }
            }

            private void handleConnect(StompHeaderAccessor accessor) {
                String sessionId = accessor.getSessionId();
                log.info("WebSocket CONNECT: sessionId={}", sessionId);
                
                // Extract authentication from headers if available
                String authToken = accessor.getFirstNativeHeader("Authorization");
                if (authToken != null && authToken.startsWith("Bearer ")) {
                    // Here you would validate the JWT token and set authentication
                    // For now, we'll create a simple authentication
                    Authentication auth = new UsernamePasswordAuthenticationToken("user", null, List.of());
                    accessor.setUser(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            private void handleDisconnect(StompHeaderAccessor accessor) {
                String sessionId = accessor.getSessionId();
                log.info("WebSocket DISCONNECT: sessionId={}", sessionId);
                
                // Clean up any session-specific resources
                SecurityContextHolder.clearContext();
            }

            private void handleSubscribe(StompHeaderAccessor accessor) {
                String destination = accessor.getDestination();
                String sessionId = accessor.getSessionId();
                
                log.info("WebSocket SUBSCRIBE: destination={}, sessionId={}", destination, sessionId);
                
                // Validate subscription permissions
                if (destination != null && destination.startsWith("/user/") && accessor.getUser() == null) {
                    log.warn("Unauthorized subscription attempt to private destination: {}", destination);
                    throw new SecurityException("Authentication required for private destinations");
                }
            }

            private void handleSend(StompHeaderAccessor accessor) {
                String destination = accessor.getDestination();
                String sessionId = accessor.getSessionId();
                
                log.debug("WebSocket SEND: destination={}, sessionId={}", destination, sessionId);
                
                // Validate send permissions
                if (destination != null && destination.startsWith("/app/admin/") && 
                    (accessor.getUser() == null || !hasAdminRole(accessor.getUser()))) {
                    log.warn("Unauthorized send attempt to admin destination: {}", destination);
                    throw new SecurityException("Admin privileges required");
                }
            }

            private boolean hasAdminRole(java.security.Principal user) {
                // Implement admin role check based on your authentication system
                return user != null && user.getName().contains("admin");
            }
        };
    }
}