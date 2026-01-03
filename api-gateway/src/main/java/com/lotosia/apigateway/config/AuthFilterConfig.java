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
            HttpCookie accessTokenCookie = exchange.getRequest().getCookies()
                    .getFirst("accessToken");

            if (accessTokenCookie != null && !accessTokenCookie.getValue().isEmpty()) {
                String token = accessTokenCookie.getValue();

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
