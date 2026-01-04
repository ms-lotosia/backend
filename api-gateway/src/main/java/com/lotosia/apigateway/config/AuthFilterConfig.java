package com.lotosia.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class AuthFilterConfig {

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private BlockManager blockManager;

    @Autowired
    private JwtProcessor jwtProcessor;

    @Bean
    public GlobalFilter authFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethod().name();
            String clientIP = getClientIP(exchange.getRequest());

            return rateLimiter.checkRateLimit(clientIP, path, method)
                    .flatMap(result -> {
                        if (result == -1) {
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                            exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
                            exchange.getResponse().getHeaders().add("Retry-After", "60");
                            return exchange.getResponse().setComplete();
                        }

                        return proceedWithAuth(exchange, chain, path, method, clientIP);
                    });
        };
    }

    private Mono<Void> proceedWithAuth(ServerWebExchange exchange, GatewayFilterChain chain, String path, String method, String clientIP) {
        String token = extractToken(exchange);

        boolean requiresAuth = jwtProcessor.isAdminRoute(path) ||
                              path.equals("/api/v1/auth/me") ||
                              path.equals("/api/v1/auth/logout");

        if (requiresAuth && (token == null || token.isEmpty())) {
            Mono<Void> recordMono = path.equals("/api/v1/auth/verify-otp") ?
                Mono.empty() : blockManager.recordFailedAttempt(clientIP, null).then();
            return recordMono.then(Mono.fromRunnable(() -> {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            }));
        }

        if (token != null && !token.isEmpty()) {
            return jwtProcessor.validateTokenForGateway(token)
                    .flatMap(validation -> {
                        if (!validation.valid) {
                            if (requiresAuth) {
                                Mono<Void> recordMono = path.equals("/api/v1/auth/verify-otp") ?
                                    Mono.empty() : blockManager.recordFailedAttempt(clientIP, null).then();
                                return recordMono.then(Mono.fromRunnable(() -> {
                                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                })).then(exchange.getResponse().setComplete());
                            } else {
                                return chain.filter(exchange);
                            }
                        }

                        return blockManager.isBlocked(clientIP, validation.email)
                                .flatMap(blocked -> {
                                    if (blocked) {
                                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                        return exchange.getResponse().setComplete();
                                    }

                                    ServerWebExchange modifiedExchange = exchange.mutate()
                                            .request(exchange.getRequest().mutate()
                                                    .header("Authorization", "Bearer " + token)
                                                    .header("X-User-Email", validation.email)
                                                    .header("X-User-Id", validation.userId != null ? validation.userId.toString() : "")
                                                    .header("X-User-Roles", validation.roles != null ? String.join(",", validation.roles) : "")
                                                    .build())
                                            .build();

                                    return chain.filter(modifiedExchange);
                                });
                    });
        }

        return chain.filter(exchange);
    }

    private String extractToken(ServerWebExchange exchange) {
        HttpCookie accessTokenCookie = exchange.getRequest().getCookies().getFirst("accessToken");
        if (accessTokenCookie != null && !accessTokenCookie.getValue().isEmpty()) {
            return accessTokenCookie.getValue();
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
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
    public GlobalFilter securityHeadersFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethod().name();

            exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");
            exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");
            exchange.getResponse().getHeaders().add("X-XSS-Protection", "1; mode=block");
            exchange.getResponse().getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
            exchange.getResponse().getHeaders().add("Permissions-Policy", "geolocation=(), microphone=(), camera()");

            exchange.getResponse().getHeaders().add("Content-Security-Policy",
                    "default-src 'self' http: https:; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' http: https:; " +
                    "style-src 'self' 'unsafe-inline' http: https:; " +
                    "img-src 'self' data: http: https:; " +
                    "font-src 'self' http: https: ws: wss:; " +
                    "connect-src 'self' http: https: ws: wss:; " +
                    "frame-ancestors 'none';");

            exchange.getResponse().getHeaders().add("Server", "");

            ServerWebExchange modifiedExchange = exchange;
            boolean isCsrfExemptedPath = path.equals("/api/v1/auth/login") ||
                                        path.equals("/api/v1/auth/request-otp") ||
                                        path.equals("/api/v1/auth/verify-otp") ||
                                        path.equals("/api/v1/auth/send-reset-password-link") ||
                                        path.equals("/api/v1/auth/reset-password");

            String cookieCsrfToken = getCsrfTokenFromCookies(exchange.getRequest());
            if (cookieCsrfToken != null && (isStateChangingMethod(method) || (method.equals("GET") && path.equals("/api/v1/auth/me"))) && path.startsWith("/api/v1/") && !isCsrfExemptedPath) {
                String requestCsrfToken = exchange.getRequest().getHeaders().getFirst("X-CSRF-Token");

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
        HttpCookie csrfCookie = request.getCookies().getFirst("csrfTokenHttpOnly");
        return csrfCookie != null ? csrfCookie.getValue() : null;
    }
}