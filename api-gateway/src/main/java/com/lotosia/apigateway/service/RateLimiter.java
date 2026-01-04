package com.lotosia.apigateway.service;

import com.lotosia.apigateway.config.RateLimitConfig;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

@Component
public class RateLimiter {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RateLimitConfig rateLimitConfig;

    private static final RedisScript<Long> RATE_LIMIT_SCRIPT = RedisScript.of(
        "local key = KEYS[1] " +
        "local limit = tonumber(ARGV[1]) " +
        "local window = tonumber(ARGV[2]) " +
        "local current = redis.call('INCR', key) " +
        "if current == 1 then " +
        "    redis.call('PEXPIRE', key, window) " +
        "end " +
        "if current <= limit then " +
        "    return current " +
        "else " +
        "    return -1 " +
        "end",
        Long.class
    );

    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);

    public RateLimiter(ReactiveRedisTemplate<String, String> redisTemplate, RateLimitConfig rateLimitConfig) {
        this.redisTemplate = redisTemplate;
        this.rateLimitConfig = rateLimitConfig;
    }

    public Mono<Long> checkRateLimit(String clientIP, String path, String method) {
        boolean isRead = "GET".equals(method);
        String key;
        String rateLimitKey = rateLimitConfig.getRateLimitKey(path, method);
        int limit = rateLimitConfig.getLimit(rateLimitKey);
        long window = RATE_WINDOW.toMillis();

        if (isRead) {
            String category = rateLimitConfig.getReadCategory(path);
            key = "ratelimit:get:" + clientIP + ":" + category;
        } else {
            String normalizedPath = normalizePathForRateLimit(path);
            key = "ratelimit:write:" + clientIP + ":" + normalizedPath;
        }

        return redisTemplate.execute(RATE_LIMIT_SCRIPT, Collections.singletonList(key),
                Arrays.asList(String.valueOf(limit), String.valueOf(window)))
                .single()
                .defaultIfEmpty(0L);
    }

    private String normalizePathForRateLimit(String path) {
        if (path.startsWith("/api/v1/auth/")) {
            return path;
        }

        if (path.startsWith("/api/v1/admin/")) {
            return path.replaceAll("/\\d+", "/*");
        }

        return path
                .replaceAll("/\\d+", "/*")
                .replaceAll("/[a-f0-9]{24}", "/*");
    }

}
