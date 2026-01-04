package com.lotosia.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.Duration;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
public class AuthFilterConfig {

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

    public AuthFilterConfig(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String SECRET_KEY = "my-hardcoded-secret-key-for-testing-purposes";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private String generateJti() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static final int AUTH_RATE_LIMIT = 5;
    private static final int OTP_RATE_LIMIT = 3;
    private static final int ADMIN_RATE_LIMIT = 10;
    private static final int GENERAL_RATE_LIMIT = 100;

    private static final int USER_BLOCK_THRESHOLD = 5;     // Block user after 5 failed attempts
    private static final int IP_BLOCK_THRESHOLD = 50;     // Block IP after 50 failed attempts (much higher)
    private static final Duration USER_BLOCK_DURATION = Duration.ofMinutes(15);  // Shorter for users
    private static final Duration IP_BLOCK_DURATION = Duration.ofHours(1);      // Longer for IPs
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
        } else if (path.startsWith("/api/v1/admin/create-admin")) {
            return 2;
        } else if (path.startsWith("/api/v1/admin/")) {
            return ADMIN_RATE_LIMIT;
        }
        return GENERAL_RATE_LIMIT;
    }

    public String getRateLimitInfo(String path) {
        int limit = getRateLimit(path);
        String category;
        if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/verify-otp")) {
            category = "AUTH";
        } else if (path.startsWith("/api/v1/auth/request-otp")) {
            category = "OTP";
        } else if (path.startsWith("/api/v1/admin/")) {
            category = "ADMIN";
        } else {
            category = "GENERAL";
        }
        return category + ":" + limit + "/minute";
    }
    /**
     * Normalizes paths for rate limiting to prevent bypass attacks.
     *
     * Strategy:
     * - Auth endpoints: Keep full path (strict rate limiting per endpoint)
     * - Admin endpoints: Normalize IDs (e.g., /api/v1/admin/users/123 → /api/v1/admin/users/*)
     * - General APIs: Normalize numeric IDs and MongoDB ObjectIds
     *
     * This prevents attackers from bypassing rate limits by varying path parameters.
     */
    private String normalizePathForRateLimit(String path) {
        // Auth endpoints - strict rate limiting per specific endpoint
        if (path.startsWith("/api/v1/auth/")) {
            return path;
        }

        // Admin endpoints - normalize to prevent ID-based bypass
        if (path.startsWith("/api/v1/admin/")) {
            return path.replaceAll("/\\d+", "/*");
        }

        // General API endpoints - normalize common ID patterns
        return path
                .replaceAll("/\\d+", "/*")           // Numeric IDs
                .replaceAll("/[a-f0-9]{24}", "/*"); // MongoDB ObjectIds
    }

    private Mono<Long> checkRateLimit(String clientIP, String path) {
        String normalizedPath = normalizePathForRateLimit(path);
        String key = "ratelimit:" + clientIP + ":" + normalizedPath;
        int limit = getRateLimit(path);
        long window = RATE_WINDOW.toMillis();

        return redisTemplate.execute(RATE_LIMIT_SCRIPT, Collections.singletonList(key),
                Arrays.asList(String.valueOf(limit), String.valueOf(window)))
                .single()
                .defaultIfEmpty(0L);
    }
    private Mono<Boolean> isBlocked(String clientIP, String userEmail) {
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
    private Mono<Long> recordFailedAttempt(String clientIP, String userEmail) {
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

            return checkRateLimit(clientIP, path)
                    .flatMap(result -> {
                        int limit = getRateLimit(path);
                        if (result == -1) {
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
                            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                            exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
                            exchange.getResponse().getHeaders().add("Retry-After", "60");
                            return exchange.getResponse().setComplete();
                        }

                        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
                        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(limit - result));

                        return proceedWithAuth(exchange, chain, path, clientIP);
                    });
        };
    }

    private Mono<Void> proceedWithAuth(ServerWebExchange exchange, GatewayFilterChain chain, String path, String clientIP) {
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
            Mono<Void> recordMono = path.equals("/api/v1/auth/verify-otp") ?
                Mono.empty() : recordFailedAttempt(clientIP, null).then(); // No user context yet
            return recordMono.then(Mono.fromRunnable(() -> {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            }));
        }

        if (token != null && !token.isEmpty()) {
            String blacklistKey = "blacklist:" + token;
            return redisTemplate.hasKey(blacklistKey)
                    .flatMap(isBlacklisted -> {
                        if (Boolean.TRUE.equals(isBlacklisted)) {
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
                            String jti = claims.get("jti", String.class);
                            @SuppressWarnings("unchecked")
                            List<String> roles = claims.get("roles", List.class);

                            if (jti != null) {
                                String blacklistKey = "blacklist:jti:" + jti;
                                return redisTemplate.hasKey(blacklistKey)
                                        .flatMap(isBlacklisted -> {
                                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                                return exchange.getResponse().setComplete();
                                            }
                            return processValidToken(exchange, chain, path, clientIP, email, userId, roles);
                        });
                    } else {
                        return processValidToken(exchange, chain, path, clientIP, email, userId, roles);
                    }

                        } catch (Exception e) {
                            Mono<Void> recordMono = (requiresAuth && !path.equals("/api/v1/auth/verify-otp")) ?
                                recordFailedAttempt(clientIP, null).then() : Mono.empty(); // No user context on JWT error
                            return recordMono.then(Mono.fromRunnable(() -> {
                                if (requiresAuth) {
                                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                }
                            })).then(requiresAuth ? exchange.getResponse().setComplete() : chain.filter(exchange));
                        }
                    });
        }

        return chain.filter(exchange);
    }

    private Mono<Void> processValidToken(ServerWebExchange exchange, GatewayFilterChain chain,
                                        String path, String clientIP, String email,
                                        Long userId, List<String> roles) {
        return isBlocked(clientIP, email)
                .flatMap(blocked -> {
                    if (blocked) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    if (isAdminRoute(path) && (roles == null || !roles.contains("ADMIN"))) {
                        return recordFailedAttempt(clientIP, email).then(Mono.fromRunnable(() -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        }));
                    }
                    String csrfToken = generateJti();
                    exchange.getResponse().getHeaders().add("Set-Cookie",
                            "csrfToken=" + csrfToken +
                            "; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400");

                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("Authorization", "Bearer " + exchange.getRequest().getCookies()
                                            .getFirst("accessToken").getValue())
                                    .header("X-User-Email", email)
                                    .header("X-User-Id", userId != null ? userId.toString() : "")
                                    .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                                    .header("X-CSRF-Token", csrfToken)
                                    .build())
                            .build();

                    return chain.filter(modifiedExchange);
                });
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

            exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");
            exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");
            exchange.getResponse().getHeaders().add("X-XSS-Protection", "1; mode=block");
            exchange.getResponse().getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
            exchange.getResponse().getHeaders().add("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

            exchange.getResponse().getHeaders().add("Content-Security-Policy",
                    "default-src 'self' http: https:; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' http: https:; " +
                    "style-src 'self' 'unsafe-inline' http: https:; " +
                    "img-src 'self' data: http: https:; " +
                    "font-src 'self' http: https:; " +
                    "connect-src 'self' http: https: ws: wss:; " +
                    "frame-ancestors 'none';");

            if (csrfToken != null) {
                exchange.getResponse().getHeaders().add("X-CSRF-Token", csrfToken);
            }

            exchange.getResponse().getHeaders().add("Server", "");

            ServerWebExchange modifiedExchange = exchange;
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
