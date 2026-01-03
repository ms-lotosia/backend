package com.lotosia.apigateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class AuthFilterConfig {

    @Bean
    public GlobalFilter authFilter() {
        return (exchange, chain) -> {
            // Extract access token from cookies
            HttpCookie accessTokenCookie = exchange.getRequest().getCookies()
                    .getFirst("accessToken");

            if (accessTokenCookie != null && !accessTokenCookie.getValue().isEmpty()) {
                // Add token to Authorization header for downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header("Authorization", "Bearer " + accessTokenCookie.getValue())
                                .build())
                        .build();
                return chain.filter(modifiedExchange);
            }

            return chain.filter(exchange);
        };
    }
}
