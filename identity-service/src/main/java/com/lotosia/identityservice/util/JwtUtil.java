package com.lotosia.identityservice.util;

import com.lotosia.identityservice.service.RedisTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: nijataghayev
 */

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "my-hardcoded-secret-key-for-testing-purposes";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private static final long JWT_EXPIRATION = 24 * 60 * 60 * 1000L;
    private static final long REFRESH_EXPIRATION = 24 * 60 * 60 * 1000L;

    private final RedisTokenService redisTokenService;
    private final RedisTemplate<String, String> redisTemplate;


    public JwtUtil(RedisTokenService redisTokenService, RedisTemplate<String, String> redisTemplate) {
        this.redisTokenService = redisTokenService;
        this.redisTemplate = redisTemplate;
    }

    public String createTokenWithRole(String username, Set<String> roles) {
        String token = Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();

        redisTemplate.opsForValue().set(username, token, JWT_EXPIRATION, TimeUnit.MILLISECONDS);
        return token;
    }

    public String createRefreshToken(String username) {
        String refreshToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();

        redisTemplate.opsForValue().set(username + ":refresh", refreshToken, REFRESH_EXPIRATION, TimeUnit.MILLISECONDS);
        return refreshToken;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(token);
            String username = getEmailFromToken(token);
            String storedToken = redisTemplate.opsForValue().get(username);
            return storedToken != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void invalidateToken(String email) {
        redisTemplate.delete(email);
        redisTemplate.delete(email + ":refresh");
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public long getJwtExpiration() {
        return JWT_EXPIRATION;
    }

    public long getRefreshExpiration() {
        return REFRESH_EXPIRATION;
    }
}
