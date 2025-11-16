package com.lotosia.identityservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveToken(String token, String userId, long expiration) {
        redisTemplate.opsForValue().set(token, userId, expiration, TimeUnit.MILLISECONDS);
    }

    public String getUserIdFromToken(String token) {
        return (String) redisTemplate.opsForValue().get(token);
    }

    public void deleteToken(String token) {
        redisTemplate.delete(token);
    }

    public boolean isTokenValid(String token) {
        return redisTemplate.hasKey(token);
    }
}
