package com.example.demo.common.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for Redis cache operations.
 * 
 * Provides convenient methods for common caching operations
 * with proper error handling and logging.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Cache key prefixes for different data types
     */
    public static final String USER_CACHE_PREFIX = "user:";
    public static final String EMPLOYEE_CACHE_PREFIX = "employee:";
    public static final String DEPARTMENT_CACHE_PREFIX = "department:";
    public static final String POSITION_CACHE_PREFIX = "position:";
    public static final String PERMISSION_CACHE_PREFIX = "permission:";
    public static final String SESSION_CACHE_PREFIX = "session:";
    public static final String EMAIL_TEMPLATE_CACHE_PREFIX = "email_template:";
    public static final String JWT_BLACKLIST_PREFIX = "jwt_blacklist:";

    /**
     * Stores a value in cache with expiration
     */
    public void put(String key, Object value, Duration expiration) {
        try {
            redisTemplate.opsForValue().set(key, value, expiration);
            log.debug("Cached value with key: {} for duration: {}", key, expiration);
        } catch (Exception e) {
            log.error("Failed to cache value with key: {}", key, e);
        }
    }

    /**
     * Stores a value in cache with default expiration (10 minutes)
     */
    public void put(String key, Object value) {
        put(key, value, Duration.ofMinutes(10));
    }

    /**
     * Retrieves a value from cache
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && type.isInstance(value)) {
                log.debug("Cache hit for key: {}", key);
                return (T) value;
            }
            log.debug("Cache miss for key: {}", key);
            return null;
        } catch (Exception e) {
            log.error("Failed to retrieve value from cache with key: {}", key, e);
            return null;
        }
    }

    /**
     * Retrieves a value from cache as Object
     */
    public Object get(String key) {
        return get(key, Object.class);
    }

    /**
     * Checks if a key exists in cache
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Failed to check existence of key: {}", key, e);
            return false;
        }
    }

    /**
     * Removes a value from cache
     */
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Evicted cache entry with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to evict cache entry with key: {}", key, e);
        }
    }

    /**
     * Removes multiple values from cache
     */
    public void evict(Collection<String> keys) {
        try {
            redisTemplate.delete(keys);
            log.debug("Evicted {} cache entries", keys.size());
        } catch (Exception e) {
            log.error("Failed to evict cache entries", e);
        }
    }

    /**
     * Removes all cache entries matching a pattern
     */
    public void evictByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Evicted {} cache entries matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Failed to evict cache entries by pattern: {}", pattern, e);
        }
    }

    /**
     * Sets expiration time for a key
     */
    public void expire(String key, Duration expiration) {
        try {
            redisTemplate.expire(key, expiration);
            log.debug("Set expiration for key: {} to: {}", key, expiration);
        } catch (Exception e) {
            log.error("Failed to set expiration for key: {}", key, e);
        }
    }

    /**
     * Gets the remaining time to live for a key
     */
    public Duration getTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? Duration.ofSeconds(ttl) : Duration.ZERO;
        } catch (Exception e) {
            log.error("Failed to get TTL for key: {}", key, e);
            return Duration.ZERO;
        }
    }

    /**
     * Increments a numeric value in cache
     */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Failed to increment value for key: {}", key, e);
            return null;
        }
    }

    /**
     * Increments a numeric value by a specific amount
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Failed to increment value for key: {} by: {}", key, delta, e);
            return null;
        }
    }

    /**
     * Adds a value to a set
     */
    public void addToSet(String key, Object... values) {
        try {
            redisTemplate.opsForSet().add(key, values);
            log.debug("Added {} values to set with key: {}", values.length, key);
        } catch (Exception e) {
            log.error("Failed to add values to set with key: {}", key, e);
        }
    }

    /**
     * Removes a value from a set
     */
    public void removeFromSet(String key, Object... values) {
        try {
            redisTemplate.opsForSet().remove(key, values);
            log.debug("Removed {} values from set with key: {}", values.length, key);
        } catch (Exception e) {
            log.error("Failed to remove values from set with key: {}", key, e);
        }
    }

    /**
     * Checks if a value is in a set
     */
    public boolean isInSet(String key, Object value) {
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(key, value);
            return isMember != null && isMember;
        } catch (Exception e) {
            log.error("Failed to check set membership for key: {}", key, e);
            return false;
        }
    }

    /**
     * Gets all members of a set
     */
    public Set<Object> getSetMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Failed to get set members for key: {}", key, e);
            return Set.of();
        }
    }

    /**
     * Adds a value to a list (left push)
     */
    public void addToList(String key, Object... values) {
        try {
            redisTemplate.opsForList().leftPushAll(key, values);
            log.debug("Added {} values to list with key: {}", values.length, key);
        } catch (Exception e) {
            log.error("Failed to add values to list with key: {}", key, e);
        }
    }

    /**
     * Gets a range of values from a list
     */
    public List<Object> getListRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Failed to get list range for key: {}", key, e);
            return List.of();
        }
    }

    /**
     * Gets the size of a list
     */
    public Long getListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("Failed to get list size for key: {}", key, e);
            return 0L;
        }
    }

    /**
     * Utility methods for generating cache keys
     */
    @UtilityClass
    public static class KeyGenerator {
        
        public static String userKey(Long userId) {
            return USER_CACHE_PREFIX + userId;
        }
        
        public static String employeeKey(Long employeeId) {
            return EMPLOYEE_CACHE_PREFIX + employeeId;
        }
        
        public static String departmentKey(Long departmentId) {
            return DEPARTMENT_CACHE_PREFIX + departmentId;
        }
        
        public static String positionKey(Long positionId) {
            return POSITION_CACHE_PREFIX + positionId;
        }
        
        public static String permissionKey(String username) {
            return PERMISSION_CACHE_PREFIX + username;
        }
        
        public static String sessionKey(String sessionId) {
            return SESSION_CACHE_PREFIX + sessionId;
        }
        
        public static String emailTemplateKey(String templateCode) {
            return EMAIL_TEMPLATE_CACHE_PREFIX + templateCode;
        }
        
        public static String jwtBlacklistKey(String jti) {
            return JWT_BLACKLIST_PREFIX + jti;
        }
    }
}