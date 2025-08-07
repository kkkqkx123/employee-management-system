package com.example.demo.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import java.util.Set;

/**
 * Redis test utilities for managing Redis state during tests.
 * Provides methods for cleaning up Redis data and managing test isolation.
 */
public class RedisTestUtil implements TestExecutionListener {

    /**
     * Clean all Redis data
     */
    public static void cleanRedisData(RedisTemplate<String, Object> redisTemplate) {
        if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
            try {
                Set<String> keys = redisTemplate.keys("*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                // Ignore Redis connection errors in tests
            }
        }
    }

    /**
     * Clean Redis data by pattern
     */
    public static void cleanRedisByPattern(RedisTemplate<String, Object> redisTemplate, String pattern) {
        if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
            try {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                // Ignore Redis connection errors in tests
            }
        }
    }

    /**
     * Check if Redis is available
     */
    public static boolean isRedisAvailable(RedisTemplate<String, Object> redisTemplate) {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set test data in Redis
     */
    public static void setTestData(RedisTemplate<String, Object> redisTemplate, String key, Object value) {
        if (isRedisAvailable(redisTemplate)) {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * Get test data from Redis
     */
    public static Object getTestData(RedisTemplate<String, Object> redisTemplate, String key) {
        if (isRedisAvailable(redisTemplate)) {
            return redisTemplate.opsForValue().get(key);
        }
        return null;
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        try {
            RedisTemplate<String, Object> redisTemplate = testContext.getApplicationContext()
                    .getBean("testRedisTemplate", RedisTemplate.class);
            cleanRedisData(redisTemplate);
        } catch (Exception e) {
            // Ignore if Redis is not available in test context
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        try {
            RedisTemplate<String, Object> redisTemplate = testContext.getApplicationContext()
                    .getBean("testRedisTemplate", RedisTemplate.class);
            cleanRedisData(redisTemplate);
        } catch (Exception e) {
            // Ignore if Redis is not available in test context
        }
    }
}