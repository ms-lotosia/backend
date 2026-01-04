package com.lotosia.apigateway.util;

import org.springframework.http.server.reactive.ServerHttpResponse;

public class SecurityHeaders {

    private SecurityHeaders() {
        // Utility class
    }

    public static void addSecurityHeaders(ServerHttpResponse response) {
        response.getHeaders().add("X-Content-Type-Options", "nosniff");
        response.getHeaders().add("X-Frame-Options", "DENY");
        response.getHeaders().add("X-XSS-Protection", "1; mode=block");
        response.getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
        response.getHeaders().add("Permissions-Policy", "geolocation=(), microphone=(), camera()");
        response.getHeaders().add("Content-Security-Policy",
                "default-src 'self' http: https:; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval' http: https:; " +
                "style-src 'self' 'unsafe-inline' http: https:; " +
                "img-src 'self' data: http: https:; " +
                "font-src 'self' http: https: ws: wss:; " +
                "connect-src 'self' http: https: ws: wss:; " +
                "frame-ancestors 'none';");
        response.getHeaders().add("Server", "");
    }
}
