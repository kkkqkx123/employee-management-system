package com.example.demo.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ListOperations;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CacheUtil class.
 */
@ExtendWith(MockitoExtension.class)
class CacheUtilTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private ListOperations<String, Object> listOperations;

    private CacheUtil cacheUtil;

    @BeforeEach
    void setUp() {
        cacheUtil = new CacheUtil(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void testPutWithExpiration() {
        String key = "test-key";
        String value = "test-value";
        Duration expiration = Duration.ofMinutes(5);

        cacheUtil.put(key, value, expiration);

        verify(valueOperations).set(key, value, expiration);
    }

    @Test
    void testPutWithDefaultExpiration() {
        String key = "test-key";
        String value = "test-value";

        cacheUtil.put(key, value);

        verify(valueOperations).set(eq(key), eq(value), eq(Duration.ofMinutes(10)));
    }

    @Test
    void testPutWithException() {
        String key = "test-key";
        String value = "test-value";
        Duration expiration = Duration.ofMinutes(5);

        doThrow(new RuntimeException("Redis error")).when(valueOperations).set(key, value, expiration);

        // Should not throw exception, just log error
        cacheUtil.put(key, value, expiration);

        verify(valueOperations).set(key, value, expiration);
    }

    @Test
    void testGetWithType() {
        String key = "test-key";
        String value = "test-value";

        when(valueOperations.get(key)).thenReturn(value);

        String result = cacheUtil.get(key, String.class);

        assertThat(result).isEqualTo(value);
        verify(valueOperations).get(key);
    }

    @Test
    void testGetWithTypeWrongType() {
        String key = "test-key";
        String value = "test-value";

        when(valueOperations.get(key)).thenReturn(value);

        Integer result = cacheUtil.get(key, Integer.class);

        assertThat(result).isNull();
        verify(valueOperations).get(key);
    }

    @Test
    void testGetWithTypeNull() {
        String key = "test-key";

        when(valueOperations.get(key)).thenReturn(null);

        String result = cacheUtil.get(key, String.class);

        assertThat(result).isNull();
        verify(valueOperations).get(key);
    }

    @Test
    void testGetAsObject() {
        String key = "test-key";
        String value = "test-value";

        when(valueOperations.get(key)).thenReturn(value);

        Object result = cacheUtil.get(key);

        assertThat(result).isEqualTo(value);
        verify(valueOperations).get(key);
    }

    @Test
    void testExists() {
        String key = "test-key";

        when(redisTemplate.hasKey(key)).thenReturn(true);

        boolean result = cacheUtil.exists(key);

        assertThat(result).isTrue();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void testExistsReturnsFalse() {
        String key = "test-key";

        when(redisTemplate.hasKey(key)).thenReturn(false);

        boolean result = cacheUtil.exists(key);

        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void testExistsWithNull() {
        String key = "test-key";

        when(redisTemplate.hasKey(key)).thenReturn(null);

        boolean result = cacheUtil.exists(key);

        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void testEvictSingleKey() {
        String key = "test-key";

        cacheUtil.evict(key);

        verify(redisTemplate).delete(key);
    }

    @Test
    void testEvictMultipleKeys() {
        List<String> keys = Arrays.asList("key1", "key2", "key3");

        cacheUtil.evict(keys);

        verify(redisTemplate).delete(keys);
    }

    @Test
    void testEvictByPattern() {
        String pattern = "user:*";
        Set<String> keys = Set.of("user:1", "user:2", "user:3");

        when(redisTemplate.keys(pattern)).thenReturn(keys);

        cacheUtil.evictByPattern(pattern);

        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(keys);
    }

    @Test
    void testEvictByPatternNoKeys() {
        String pattern = "user:*";

        when(redisTemplate.keys(pattern)).thenReturn(Set.of());

        cacheUtil.evictByPattern(pattern);

        verify(redisTemplate).keys(pattern);
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    void testExpire() {
        String key = "test-key";
        Duration expiration = Duration.ofMinutes(5);

        cacheUtil.expire(key, expiration);

        verify(redisTemplate).expire(key, expiration);
    }

    @Test
    void testGetTtl() {
        String key = "test-key";
        Long ttlSeconds = 300L;

        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(ttlSeconds);

        Duration result = cacheUtil.getTtl(key);

        assertThat(result).isEqualTo(Duration.ofSeconds(300));
        verify(redisTemplate).getExpire(key, TimeUnit.SECONDS);
    }

    @Test
    void testGetTtlExpired() {
        String key = "test-key";

        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(-1L);

        Duration result = cacheUtil.getTtl(key);

        assertThat(result).isEqualTo(Duration.ZERO);
        verify(redisTemplate).getExpire(key, TimeUnit.SECONDS);
    }

    @Test
    void testIncrement() {
        String key = "counter";
        Long expectedValue = 1L;

        when(valueOperations.increment(key)).thenReturn(expectedValue);

        Long result = cacheUtil.increment(key);

        assertThat(result).isEqualTo(expectedValue);
        verify(valueOperations).increment(key);
    }

    @Test
    void testIncrementWithDelta() {
        String key = "counter";
        long delta = 5L;
        Long expectedValue = 10L;

        when(valueOperations.increment(key, delta)).thenReturn(expectedValue);

        Long result = cacheUtil.increment(key, delta);

        assertThat(result).isEqualTo(expectedValue);
        verify(valueOperations).increment(key, delta);
    }

    @Test
    void testAddToSet() {
        String key = "test-set";
        Object[] values = {"value1", "value2", "value3"};

        cacheUtil.addToSet(key, values);

        verify(setOperations).add(key, values);
    }

    @Test
    void testRemoveFromSet() {
        String key = "test-set";
        Object[] values = {"value1", "value2"};

        cacheUtil.removeFromSet(key, values);

        verify(setOperations).remove(key, values);
    }

    @Test
    void testIsInSet() {
        String key = "test-set";
        Object value = "test-value";

        when(setOperations.isMember(key, value)).thenReturn(true);

        boolean result = cacheUtil.isInSet(key, value);

        assertThat(result).isTrue();
        verify(setOperations).isMember(key, value);
    }

    @Test
    void testIsInSetReturnsFalse() {
        String key = "test-set";
        Object value = "test-value";

        when(setOperations.isMember(key, value)).thenReturn(false);

        boolean result = cacheUtil.isInSet(key, value);

        assertThat(result).isFalse();
        verify(setOperations).isMember(key, value);
    }

    @Test
    void testGetSetMembers() {
        String key = "test-set";
        Set<Object> members = Set.of("value1", "value2", "value3");

        when(setOperations.members(key)).thenReturn(members);

        Set<Object> result = cacheUtil.getSetMembers(key);

        assertThat(result).isEqualTo(members);
        verify(setOperations).members(key);
    }

    @Test
    void testAddToList() {
        String key = "test-list";
        Object[] values = {"value1", "value2", "value3"};

        cacheUtil.addToList(key, values);

        verify(listOperations).leftPushAll(key, values);
    }

    @Test
    void testGetListRange() {
        String key = "test-list";
        long start = 0;
        long end = 2;
        List<Object> expectedList = Arrays.asList("value1", "value2", "value3");

        when(listOperations.range(key, start, end)).thenReturn(expectedList);

        List<Object> result = cacheUtil.getListRange(key, start, end);

        assertThat(result).isEqualTo(expectedList);
        verify(listOperations).range(key, start, end);
    }

    @Test
    void testGetListSize() {
        String key = "test-list";
        Long expectedSize = 5L;

        when(listOperations.size(key)).thenReturn(expectedSize);

        Long result = cacheUtil.getListSize(key);

        assertThat(result).isEqualTo(expectedSize);
        verify(listOperations).size(key);
    }

    @Test
    void testKeyGeneratorMethods() {
        assertThat(CacheUtil.KeyGenerator.userKey(123L)).isEqualTo("user:123");
        assertThat(CacheUtil.KeyGenerator.employeeKey(456L)).isEqualTo("employee:456");
        assertThat(CacheUtil.KeyGenerator.departmentKey(789L)).isEqualTo("department:789");
        assertThat(CacheUtil.KeyGenerator.positionKey(101L)).isEqualTo("position:101");
        assertThat(CacheUtil.KeyGenerator.permissionKey("admin")).isEqualTo("permission:admin");
        assertThat(CacheUtil.KeyGenerator.sessionKey("session123")).isEqualTo("session:session123");
        assertThat(CacheUtil.KeyGenerator.emailTemplateKey("welcome")).isEqualTo("email_template:welcome");
        assertThat(CacheUtil.KeyGenerator.jwtBlacklistKey("jti123")).isEqualTo("jwt_blacklist:jti123");
    }

    @Test
    void testConstants() {
        assertThat(CacheUtil.USER_CACHE_PREFIX).isEqualTo("user:");
        assertThat(CacheUtil.EMPLOYEE_CACHE_PREFIX).isEqualTo("employee:");
        assertThat(CacheUtil.DEPARTMENT_CACHE_PREFIX).isEqualTo("department:");
        assertThat(CacheUtil.POSITION_CACHE_PREFIX).isEqualTo("position:");
        assertThat(CacheUtil.PERMISSION_CACHE_PREFIX).isEqualTo("permission:");
        assertThat(CacheUtil.SESSION_CACHE_PREFIX).isEqualTo("session:");
        assertThat(CacheUtil.EMAIL_TEMPLATE_CACHE_PREFIX).isEqualTo("email_template:");
        assertThat(CacheUtil.JWT_BLACKLIST_PREFIX).isEqualTo("jwt_blacklist:");
    }
}