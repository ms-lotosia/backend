package com.lotosia.identityservice.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ControllerUtils {

    private ControllerUtils() {
    }

    public static ResponseEntity<Map<String, String>> successResponse(String message) {
        return ResponseEntity.ok(Map.of("message", message));
    }

    public static ResponseEntity<Map<String, String>> errorResponse(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }

    public static ResponseEntity<Map<String, String>> conflictResponse(String code, String message) {
        return errorResponse(code, message, HttpStatus.CONFLICT);
    }

    public static ResponseEntity<Map<String, String>> badRequestResponse(String code, String message) {
        return errorResponse(code, message, HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<Map<String, String>> unauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", message));
    }

    public static ResponseEntity<Map<String, String>> forbiddenResponse(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", message));
    }

    public static void addAuthCookies(CookieUtil cookieUtil, HttpServletResponse response, String accessToken, String refreshToken, String csrfToken) {
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);
        cookieUtil.addCsrfTokenCookie(response, csrfToken);
    }

    public static void addAuthCookies(CookieUtil cookieUtil, HttpServletResponse response, String accessToken, String refreshToken) {
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);
    }

    public static void clearAuthCookies(CookieUtil cookieUtil, HttpServletResponse response) {
        cookieUtil.clearAccessTokenCookie(response);
        cookieUtil.clearRefreshTokenCookie(response);
        cookieUtil.clearCsrfTokenCookie(response);
    }

    public static String extractToken(CookieUtil cookieUtil, HttpServletRequest request) {
        return cookieUtil.getAccessTokenFromCookies(request);
    }

    public static String extractRefreshToken(CookieUtil cookieUtil, HttpServletRequest request) {
        return cookieUtil.getRefreshTokenFromCookies(request);
    }
}
