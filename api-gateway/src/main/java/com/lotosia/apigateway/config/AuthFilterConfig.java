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
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.Duration;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Configuration
public class AuthFilterConfig {

    private final RedisTemplate<String, String> redisTemplate;

    public AuthFilterConfig(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String SECRET_KEY = "my-hardcoded-secret-key-for-testing-purposes";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private static final int AUTH_RATE_LIMIT = 5;
    private static final int OTP_RATE_LIMIT = 3;
    private static final int ADMIN_RATE_LIMIT = 10;
    private static final int GENERAL_RATE_LIMIT = 100;

    private static final int BLOCK_THRESHOLD = 10;
    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);
    private static final Duration BLOCK_DURATION = Duration.ofHours(1);

    private boolean isAdminRoute(String path) {
        return path.startsWith("/api/v1/admin/") &&
               !path.equals("/api/v1/admin/create-admin");
    }
    private int getRateLimit(String path) {
        if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/verify-otp")) {
            return AUTH_RATE_LIMIT;
        } else if (path.startsWith("/api/v1/auth/request-otp")) {
            return OTP_RATE_LIMIT;
        } else if (path.startsWith("/api/v1/admin/")) {
            return ADMIN_RATE_LIMIT;
        }
        return GENERAL_RATE_LIMIT;
    }
    private boolean isRateLimited(String clientIP, String path) {
        String key = "ratelimit:" + clientIP + ":" + path.replaceAll("/\\{[^}]*}", "/{id}");
        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount == 1) {
            redisTemplate.expire(key, RATE_WINDOW);
        }
        return currentCount > getRateLimit(path);
    }
    private boolean isBlocked(String clientIP) {
        String blockKey = "blocked:" + clientIP;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }
    private void recordFailedAttempt(String clientIP) {
        String attemptsKey = "failed_attempts:" + clientIP;
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts == 1) {
            redisTemplate.expire(attemptsKey, BLOCK_DURATION);
        }

        if (attempts >= BLOCK_THRESHOLD) {
            String blockKey = "blocked:" + clientIP;
            redisTemplate.opsForValue().set(blockKey, "1", BLOCK_DURATION);
            redisTemplate.delete(attemptsKey);
        }
    }
    private String getClientIP(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddress() != null ?
               request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Bean
    public GlobalFilter authFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            String clientIP = getClientIP(exchange.getRequest());

            if (isBlocked(clientIP)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if (isRateLimited(clientIP, path)) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }

            String token = null;
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
            boolean requiresAuth = isAdminRoute(path) ||
                                  path.equals("/api/v1/auth/me") ||
                                  path.equals("/api/v1/auth/logout");

            if (requiresAuth && (token == null || token.isEmpty())) {
                recordFailedAttempt(clientIP);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            if (token != null && !token.isEmpty()) {
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

                    if (isAdminRoute(path) && (roles == null || !roles.contains("ADMIN"))) {
                        recordFailedAttempt(clientIP);
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    String csrfToken = UUID.randomUUID().toString();

                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("Authorization", "Bearer " + token)
                                    .header("X-User-Email", email)
                                    .header("X-User-Id", userId != null ? userId.toString() : "")
                                    .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                                    .header("X-CSRF-Token", csrfToken)
                                    .build())
                            .response(exchange.getResponse().mutate()
                                    .header("Set-Cookie",
                                            "csrfToken=" + csrfToken +
                                            "; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400")
                                    .build())
                            .build();

                    return chain.filter(modifiedExchange);

                } catch (Exception e) {
                    if (requiresAuth) {
                        recordFailedAttempt(clientIP);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                }
            } else if (requiresAuth) {
                recordFailedAttempt(clientIP);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }

    @Bean
    public GlobalFilter securityHeadersFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethod().name();

            String csrfToken = null;
            if (path.startsWith("/api/v1/") && !path.contains("/login") && !path.contains("/request-otp")) {
                csrfToken = UUID.randomUUID().toString();
            }

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .response(exchange.getResponse().mutate()
                            .header("X-Content-Type-Options", "nosniff")
                            .header("X-Frame-Options", "DENY")
                            .header("X-XSS-Protection", "1; mode=block")
                            .header("Referrer-Policy", "strict-origin-when-cross-origin")
                            .header("Permissions-Policy", "geolocation=(), microphone=(), camera=()")

                            .header("Content-Security-Policy",
                                    "default-src 'self' http: https:; " +
                                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' http: https:; " +
                                    "style-src 'self' 'unsafe-inline' http: https:; " +
                                    "img-src 'self' data: http: https:; " +
                                    "font-src 'self' http: https:; " +
                                    "connect-src 'self' http: https: ws: wss:; " +
                                    "frame-ancestors 'none';")

                            .header("X-CSRF-Token", csrfToken != null ? csrfToken : "")

                            .header("Server", "")
                            .build())
                    .build();
            if (isStateChangingMethod(method) && path.startsWith("/api/v1/")) {
                String requestCsrfToken = exchange.getRequest().getHeaders().getFirst("X-CSRF-Token");
                String cookieCsrfToken = getCsrfTokenFromCookies(exchange.getRequest());

                if (requestCsrfToken == null || !requestCsrfToken.equals(cookieCsrfToken)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            return chain.filter(modifiedExchange);
        };
    }

    private boolean isStateChangingMethod(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method) || "PATCH".equals(method);
    }

    private String getCsrfTokenFromCookies(ServerHttpRequest request) {
        HttpCookie csrfCookie = request.getCookies().getFirst("csrfToken");
        return csrfCookie != null ? csrfCookie.getValue() : null;
    }
}
