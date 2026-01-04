package com.lotosia.apigateway.config;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class BlockManager {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final int USER_BLOCK_THRESHOLD = 5;
    private static final int IP_BLOCK_THRESHOLD = 50;
    private static final Duration USER_BLOCK_DURATION = Duration.ofMinutes(15);
    private static final Duration IP_BLOCK_DURATION = Duration.ofHours(1);

    public BlockManager(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> isBlocked(String clientIP, String userEmail) {
        if (userEmail != null && !userEmail.isEmpty()) {
            String userBlockKey = "blocked:user:" + userEmail;
            return redisTemplate.hasKey(userBlockKey)
                    .flatMap(userBlocked -> {
                        if (userBlocked) {
                            return Mono.just(true);
                        }
                        String ipBlockKey = "blocked:ip:" + clientIP;
                        return redisTemplate.hasKey(ipBlockKey);
                    });
        }

        String ipBlockKey = "blocked:ip:" + clientIP;
        return redisTemplate.hasKey(ipBlockKey);
    }

    public Mono<Long> recordFailedAttempt(String clientIP, String userEmail) {
        if (userEmail != null && !userEmail.isEmpty()) {
            return recordUserFailedAttempt(userEmail);
        }

        return recordIpFailedAttempt(clientIP);
    }

    private Mono<Long> recordUserFailedAttempt(String userEmail) {
        String userAttemptsKey = "failed_attempts:user:" + userEmail;
        String userBlockKey = "blocked:user:" + userEmail;

        return redisTemplate.opsForValue().increment(userAttemptsKey)
                .flatMap(attempts -> {
                    if (attempts == 1) {
                        return redisTemplate.expire(userAttemptsKey, USER_BLOCK_DURATION)
                                .thenReturn(attempts);
                    }
                    return Mono.just(attempts);
                })
                .flatMap(attempts -> {
                    if (attempts >= USER_BLOCK_THRESHOLD) {
                        return redisTemplate.opsForValue().set(userBlockKey, "1")
                                .then(redisTemplate.expire(userBlockKey, USER_BLOCK_DURATION))
                                .then(redisTemplate.delete(userAttemptsKey))
                                .thenReturn(attempts);
                    }
                    return Mono.just(attempts);
                });
    }

    private Mono<Long> recordIpFailedAttempt(String clientIP) {
        String ipAttemptsKey = "failed_attempts:ip:" + clientIP;
        String ipBlockKey = "blocked:ip:" + clientIP;

        return redisTemplate.opsForValue().increment(ipAttemptsKey)
                .flatMap(attempts -> {
                    if (attempts == 1) {
                        return redisTemplate.expire(ipAttemptsKey, IP_BLOCK_DURATION)
                                .thenReturn(attempts);
                    }
                    return Mono.just(attempts);
                })
                .flatMap(attempts -> {
                    if (attempts >= IP_BLOCK_THRESHOLD) {
                        return redisTemplate.opsForValue().set(ipBlockKey, "1")
                                .then(redisTemplate.expire(ipBlockKey, IP_BLOCK_DURATION))
                                .then(redisTemplate.delete(ipAttemptsKey))
                                .thenReturn(attempts);
                    }
                    return Mono.just(attempts);
                });
    }
}
