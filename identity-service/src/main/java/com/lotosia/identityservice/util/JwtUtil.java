package com.lotosia.identityservice.util;

import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.service.RedisTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: nijataghayev
 */

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String SECRET_KEY = "my-hardcoded-secret-key-for-testing-purposes";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private static final long JWT_EXPIRATION = 24 * 60 * 60 * 1000L;
    private static final long REFRESH_EXPIRATION = 24 * 60 * 60 * 1000L;

    private final RedisTemplate<String, String> redisTemplate;


    public String createTokenWithRole(String username, Long userId, Set<Role> roles) {
        List<String> roleNames = roles.stream().map(Role::getName).toList();

        String token = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("roles", roleNames)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();

        redisTemplate.opsForValue().set(
                "TOKEN:" + token,
                username,
                JWT_EXPIRATION,
                TimeUnit.MILLISECONDS
        );

        return token;
    }

    public String createRefreshToken(String username, Long userId) {
        String refreshToken = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();

        redisTemplate.opsForValue().set(
                username + ":refresh",
                refreshToken,
                REFRESH_EXPIRATION,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    public long getExpirationTime(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SIGNING_KEY).build().parseClaimsJws(token);
            String username = redisTemplate.opsForValue().get("TOKEN:" + token);
            return username != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void invalidateToken(String token) {
        String username = redisTemplate.opsForValue().get("TOKEN:" + token);
        if (username != null) {
            redisTemplate.delete("TOKEN:" + token);
            redisTemplate.delete(username + ":refresh");
        }
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
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

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }

    public long getJwtExpiration() {
        return JWT_EXPIRATION;
    }

    public long getRefreshExpiration() {
        return REFRESH_EXPIRATION;
    }
}
