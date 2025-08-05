package com.example.demo.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Configuration for caching and real-time features.
 * 
 * Configures Redis connection, serialization, caching, and templates
 * for both caching and real-time messaging functionality.
 */
@Configuration
@EnableCaching
@EnableRedisRepositories(basePackages = "com.example.demo.communication.chat.repository")
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    /**
     * Redis Connection Factory configuration
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);

        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig);
        factory.setValidateConnection(true);

        return factory;
    }

    /**
     * Redis Template for general Redis operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure JSON serialization
        ObjectMapper objectMapper = createObjectMapper();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Key serialization
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serialization
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * String Redis Template for simple string operations
     */
    @Bean
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache Manager configuration with different TTL for different cache types
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = createObjectMapper();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // Specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User cache - 30 minutes TTL
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Department cache - 1 hour TTL (departments change less frequently)
        cacheConfigurations.put("departments", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Position cache - 1 hour TTL
        cacheConfigurations.put("positions", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Employee cache - 15 minutes TTL
        cacheConfigurations.put("employees", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Permission cache - 1 hour TTL
        cacheConfigurations.put("permissions", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Session cache - 30 minutes TTL
        cacheConfigurations.put("sessions", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Email template cache - 2 hours TTL
        cacheConfigurations.put("email-templates", defaultConfig.entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Creates ObjectMapper for Redis JSON serialization
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Register JavaTimeModule for LocalDateTime, LocalDate support
        objectMapper.registerModule(new JavaTimeModule());

        // Configure visibility
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // Enable type information for polymorphic serialization
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        return objectMapper;
    }

    /**
     * Health check bean for Redis connectivity
     */
    @Bean
    public RedisHealthIndicator redisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        return new RedisHealthIndicator(redisTemplate);
    }

    /**
     * Custom health indicator for Redis
     */
    public static class RedisHealthIndicator {
        private final RedisTemplate<String, Object> redisTemplate;

        public RedisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        public boolean isRedisAvailable() {
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public String getRedisInfo() {
            try {
                return redisTemplate.getConnectionFactory().getConnection().info().getProperty("redis_version");
            } catch (Exception e) {
                return "Unable to retrieve Redis info";
            }
        }
    }
}