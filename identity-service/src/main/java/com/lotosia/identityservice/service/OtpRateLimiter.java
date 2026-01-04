package com.lotosia.identityservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OtpRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    public OtpRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryAcquire(String email) {
        String attemptsKey = email + ":otp_attempts";
        String lastAttemptKey = email + ":last_otp_attempt";

        long currentTime = System.currentTimeMillis();
        long fiveMinutesAgo = currentTime - (5 * 60 * 1000);
        long thirtySecondsAgo = currentTime - (30 * 1000);

        redisTemplate.opsForZSet().removeRangeByScore(attemptsKey, 0, fiveMinutesAgo);

        String lastAttemptStr = redisTemplate.opsForValue().get(lastAttemptKey);
        if (lastAttemptStr != null) {
            long lastAttemptTime = Long.parseLong(lastAttemptStr);
            if (lastAttemptTime > thirtySecondsAgo) {
                return false;
            }
        }

        Long attemptCount = redisTemplate.opsForZSet().size(attemptsKey);
        if (attemptCount != null && attemptCount >= 3) {
            return false;
        }

        redisTemplate.opsForZSet().add(attemptsKey, String.valueOf(currentTime), currentTime);
        redisTemplate.opsForValue().set(lastAttemptKey, String.valueOf(currentTime), 5, TimeUnit.MINUTES);

        return true;
    }
}
