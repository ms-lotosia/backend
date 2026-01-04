package com.lotosia.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "lotosia.rate-limit")
public class RateLimitConfig {

    private Map<String, Integer> limits = new HashMap<>();

    // Default constructor with default values
    public RateLimitConfig() {
        // Auth endpoints
        limits.put("auth.login.post", 5);
        limits.put("auth.verify-otp.post", 5);
        limits.put("auth.request-otp.post", 3);
        limits.put("auth.send-reset-password-link.post", 2);
        limits.put("auth.reset-password.post", 5);

        // Admin endpoints
        limits.put("admin.create-admin.post", 2);
        limits.put("admin.write", 30);

        // Checkout endpoints
        limits.put("checkout.write", 10);

        // General limits
        limits.put("general.write", 60);
        limits.put("general.read", 300);
    }

    public int getLimit(String key) {
        return limits.getOrDefault(key, 300); // Default to 300 if not found
    }

    public boolean isCheckoutEndpoint(String path) {
        return path.contains("/checkout") ||
               path.contains("/payments") ||
               path.contains("/orders") ||
               path.contains("/baskets") ||
               path.contains("/carts");
    }

    public String getReadCategory(String path) {
        if (path.contains("/products") || path.contains("/catalog") || path.contains("/items")) {
            return "CATALOG_GET";
        } else if (path.contains("/search") || path.contains("/filter")) {
            return "SEARCH_GET";
        } else if (path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/admin/")) {
            return "AUTH_GET";
        } else {
            return "GENERAL_GET";
        }
    }

    public String getRateLimitKey(String path, String method) {
        boolean isRead = "GET".equals(method);
        boolean isWrite = Arrays.asList("POST", "PUT", "DELETE", "PATCH").contains(method);

        if (path.startsWith("/api/v1/auth/login") && isWrite) {
            return "auth.login.post";
        } else if (path.startsWith("/api/v1/auth/verify-otp") && isWrite) {
            return "auth.verify-otp.post";
        } else if (path.startsWith("/api/v1/auth/request-otp") && isWrite) {
            return "auth.request-otp.post";
        } else if (path.equals("/api/v1/auth/send-reset-password-link") && isWrite) {
            return "auth.send-reset-password-link.post";
        } else if (path.equals("/api/v1/auth/reset-password") && isWrite) {
            return "auth.reset-password.post";
        } else if (path.startsWith("/api/v1/admin/create-admin") && isWrite) {
            return "admin.create-admin.post";
        } else if (path.startsWith("/api/v1/admin/") && isWrite) {
            return "admin.write";
        } else if (isCheckoutEndpoint(path) && isWrite) {
            return "checkout.write";
        } else if (isWrite) {
            return "general.write";
        } else if (isRead) {
            return "general.read";
        }

        return "general.read";
    }

    // Getters and setters for configuration properties binding
    public Map<String, Integer> getLimits() {
        return limits;
    }

    public void setLimits(Map<String, Integer> limits) {
        this.limits = limits;
    }
}
