package com.lotosia.apigateway.config;

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

    public RateLimiter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Long> checkRateLimit(String clientIP, String path, String method) {
        boolean isRead = "GET".equals(method);
        String key;
        int limit = getRateLimit(path, method);
        long window = RATE_WINDOW.toMillis();

        if (isRead) {
            String category = getReadCategory(path);
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

    private String getReadCategory(String path) {
        if (path.contains("/products") || path.contains("/catalog") || path.contains("/items")) {
            return "CATALOG_GET";
        } else if (path.contains("/search") || path.contains("/filter")) {
            return "SEARCH_GET";
        } else if (path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/admin/")) {
            return "AUTH_GET";
        } else {
            return "GENERAL_GET";
        }
    }

    private int getRateLimit(String path, String method) {
        boolean isRead = "GET".equals(method);
        boolean isWrite = Arrays.asList("POST", "PUT", "DELETE", "PATCH").contains(method);

        if (path.startsWith("/api/v1/auth/login") && isWrite) {
            return 5;
        } else if (path.startsWith("/api/v1/auth/verify-otp") && isWrite) {
            return 5;
        } else if (path.startsWith("/api/v1/auth/request-otp") && isWrite) {
            return 3;
        } else if (path.equals("/api/v1/auth/send-reset-password-link") && isWrite) {
            return 2; // Very restrictive - only 2 password reset requests per minute per IP
        } else if (path.equals("/api/v1/auth/reset-password") && isWrite) {
            return 5; // Allow multiple reset attempts per IP (user might try different tokens)
        } else if (path.startsWith("/api/v1/admin/create-admin") && isWrite) {
            return 2;
        } else if (path.startsWith("/api/v1/admin/") && isWrite) {
            return 30;
        } else if (isCheckoutEndpoint(path) && isWrite) {
            return 10;
        } else if (isWrite) {
            return 60;
        } else if (isRead) {
            return 300;
        }

        return 300;
    }

    private boolean isCheckoutEndpoint(String path) {
        return path.contains("/checkout") ||
               path.contains("/payments") ||
               path.contains("/orders") ||
               path.contains("/baskets") ||
               path.contains("/carts");
    }
}
