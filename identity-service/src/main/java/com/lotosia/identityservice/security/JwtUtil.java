package com.lotosia.identityservice.security;

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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String SECRET_KEY = "my-hardcoded-secret-key-for-testing-purposes";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private static final long JWT_EXPIRATION = 24 * 60 * 60 * 1000L; // 24 hours
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 days

    private final RedisTemplate<String, String> redisTemplate;
    public String createTokenWithRole(String username, Long userId, Role role) {
        List<String> roleNames = role != null ? List.of(role.getName()) : List.of();
        String jti = UUID.randomUUID().toString().replace("-", "");

        String token = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("roles", roleNames)
                .claim("jti", jti)
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

        redisTemplate.opsForValue().set(
                "refresh:" + refreshToken,
                username,
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

    public boolean validateTokenForService(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
                return false;
            }

            String jti = claims.get("jti", String.class);
            if (jti != null) {
                Boolean isBlacklisted = redisTemplate.hasKey("blacklist:jti:" + jti);
                if (Boolean.TRUE.equals(isBlacklisted)) {
                    return false;
                }
            }

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
            String refreshToken = redisTemplate.opsForValue().get(username + ":refresh");
            if (refreshToken != null) {
            redisTemplate.delete(username + ":refresh");
                redisTemplate.delete("refresh:" + refreshToken);
            }
        }

        try {
            String jti = getJtiFromToken(token);
            if (jti != null) {
                long remainingTime = getExpirationTime(token);
                if (remainingTime > 0) {
                    redisTemplate.opsForValue().set("blacklist:jti:" + jti, "1",
                        remainingTime, TimeUnit.MILLISECONDS);
                }
            }
        } catch (Exception e) {
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

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }
        return new ArrayList<>();
    }

    public String getJtiFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("jti", String.class);
    }

    public long getJwtExpiration() {
        return JWT_EXPIRATION;
    }

    public long getJwtExpirationSeconds() {
        return JWT_EXPIRATION / 1000;
    }

    public long getRefreshExpiration() {
        return REFRESH_EXPIRATION;
    }
}
