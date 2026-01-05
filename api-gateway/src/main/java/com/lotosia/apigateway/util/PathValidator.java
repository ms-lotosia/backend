package com.lotosia.apigateway.util;

import java.util.Set;

public class PathValidator {

    private static final Set<String> CSRF_EXEMPTED_PATHS = Set.of(
        "/api/v1/auth/login",
        "/api/v1/auth/request-otp",
        "/api/v1/auth/verify-otp",
        "/api/v1/auth/send-reset-password-link",
        "/api/v1/auth/reset-password",
        "/api/v1/contactUs"
    );

    private static final Set<String> AUTH_REQUIRED_PATHS = Set.of(
        "/api/v1/auth/me",
        "/api/v1/auth/logout"
    );

    private PathValidator() {
    }

    public static boolean isCsrfExempted(String path) {
        return CSRF_EXEMPTED_PATHS.contains(path);
    }

    public static boolean requiresAuth(String path) {
        return path.startsWith("/api/v1/admin/") ||
               AUTH_REQUIRED_PATHS.contains(path) ||
               isContactUsAdminEndpoint(path);
    }

    private static boolean isContactUsAdminEndpoint(String path) {
        return path.startsWith("/api/v1/contactUs/") &&
               (path.matches("/api/v1/contactUs/\\d+") || path.equals("/api/v1/contactUs/all"));
    }

    public static boolean isAdminRoute(String path) {
        return path.startsWith("/api/v1/admin/") &&
               !path.equals("/api/v1/admin/create-admin");
    }

    public static boolean startsWithApiV1(String path) {
        return path.startsWith("/api/v1/");
    }
}
