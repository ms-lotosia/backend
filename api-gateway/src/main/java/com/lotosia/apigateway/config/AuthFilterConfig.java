package com.lotosia.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Configuration
public class AuthFilterConfig {

    private static final String SECRET_KEY = "my-hardcoded-secret-key-for-testing-purposes";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @Bean
    public GlobalFilter authFilter() {
        return (exchange, chain) -> {
            String token = null;

            // Debug: Check all cookies
            String path = exchange.getRequest().getPath().value();
            if (path.contains("/me")) {
                System.out.println("API Gateway: Processing /me request");
                if (exchange.getRequest().getCookies() != null) {
                    exchange.getRequest().getCookies().forEach(cookie ->
                        System.out.println("API Gateway: Cookie found: " + cookie.getName() + " = " + cookie.getValue())
                    );
                } else {
                    System.out.println("API Gateway: No cookies found");
                }
            }

            HttpCookie accessTokenCookie = exchange.getRequest().getCookies()
                    .getFirst("accessToken");
            if (accessTokenCookie != null && !accessTokenCookie.getValue().isEmpty()) {
                token = accessTokenCookie.getValue();
                if (path.contains("/me")) {
                    System.out.println("API Gateway: Found accessToken cookie");
                }
            }

            if (token == null) {
                String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    if (path.contains("/me")) {
                        System.out.println("API Gateway: Found token in Authorization header");
                    }
                }
            }

            if (token != null && !token.isEmpty()) {

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
                    return chain.filter(exchange);
                }
            }

            return chain.filter(exchange);
        };
    }
}
