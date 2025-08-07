package com.example.demo.config;

import com.example.demo.security.security.JwtTokenProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test configuration for Spring Boot tests.
 * Provides mock beans and test-specific configurations.
 */
@TestConfiguration
public class TestConfig {

    /**
     * Mock JWT token provider for security tests
     */
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Mock mail sender for email tests
     */
    @MockitoBean
    private JavaMailSender javaMailSender;

    /**
     * Mock Redis connection factory for Redis tests
     */
    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * Password encoder for test security configuration
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Test Redis template configuration
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> testRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}