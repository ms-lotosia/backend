package com.lotosia.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.List;
import java.util.UUID;

@Component
public class JwtProcessor {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final String SECRET_KEY = "my-hardcoded-secret-key-for-testing-purposes";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public JwtProcessor(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateJti() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Mono<TokenValidationResult> validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return Mono.just(new TokenValidationResult(false, null, null, null, null));
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            Long userId = claims.get("userId", Long.class);
            String jti = claims.get("jti", String.class);
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            if (jti != null) {
                String blacklistKey = "blacklist:jti:" + jti;
                return redisTemplate.hasKey(blacklistKey)
                        .map(isBlacklisted -> {
                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                return new TokenValidationResult(false, null, null, null, null);
                            }
                            return new TokenValidationResult(true, email, userId, roles, jti);
                        });
            }

            return Mono.just(new TokenValidationResult(true, email, userId, roles, jti));

        } catch (Exception e) {
            return Mono.just(new TokenValidationResult(false, null, null, null, null));
        }
    }

    public boolean isAdminRoute(String path) {
        return path.startsWith("/api/v1/admin/") &&
               !path.equals("/api/v1/admin/create-admin");
    }

    public static class TokenValidationResult {
        public final boolean valid;
        public final String email;
        public final Long userId;
        public final List<String> roles;
        public final String jti;

        public TokenValidationResult(boolean valid, String email, Long userId, List<String> roles, String jti) {
            this.valid = valid;
            this.email = email;
            this.userId = userId;
            this.roles = roles;
            this.jti = jti;
        }
    }
}
