package com.lotosia.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;
import java.util.Arrays;

@Configuration
public class AuthFilterConfig {

    private final RedisTemplate<String, String> redisTemplate;

    public AuthFilterConfig(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String SECRET_KEY = "my-hardcoded-secret-key-for-testing-purposes";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Check if path requires admin access
    private boolean isAdminRoute(String path) {
        return path.startsWith("/api/v1/admin/") &&
               !path.equals("/api/v1/admin/create-admin"); // Allow admin creation
    }

    @Bean
    public GlobalFilter authFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            String token = null;

            // Extract token from cookie or header
            HttpCookie accessTokenCookie = exchange.getRequest().getCookies()
                    .getFirst("accessToken");
            if (accessTokenCookie != null && !accessTokenCookie.getValue().isEmpty()) {
                token = accessTokenCookie.getValue();
            }

            if (token == null) {
                String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            // Check if route requires authentication
            boolean requiresAuth = isAdminRoute(path) ||
                                  path.equals("/api/v1/auth/me") ||
                                  path.equals("/api/v1/auth/logout");

            if (requiresAuth && (token == null || token.isEmpty())) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            if (token != null && !token.isEmpty()) {
                // Check if token is blacklisted
                String blacklistKey = "blacklist:" + token;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(SIGNING_KEY)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String email = claims.getSubject();
                    Long userId = claims.get("userId", Long.class);
                    @SuppressWarnings("unchecked")
                    List<String> roles = claims.get("roles", List.class);

                    // Check admin route authorization
                    if (isAdminRoute(path) && (roles == null || !roles.contains("ADMIN"))) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("Authorization", "Bearer " + token)
                                    .header("X-User-Email", email)
                                    .header("X-User-Id", userId != null ? userId.toString() : "")
                                    .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                                    .build())
                            .build();

                    return chain.filter(modifiedExchange);

                } catch (Exception e) {
                    // Invalid token - deny access for protected routes
                    if (requiresAuth) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                }
            } else if (requiresAuth) {
                // No token provided for protected route
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }
}
