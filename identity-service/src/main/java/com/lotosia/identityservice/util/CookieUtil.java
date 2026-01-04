package com.lotosia.identityservice.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String CSRF_TOKEN_COOKIE_NAME = "csrfToken";
    private static final String ACCESS_TOKEN_PATH = "/";
    private static final String REFRESH_TOKEN_PATH = "/";
    private static final String CSRF_TOKEN_PATH = "/";
    private static final int ACCESS_TOKEN_MAX_AGE_SECONDS = 24 * 60 * 60;
    private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 7 * 24 * 60 * 60;
    private static final int CSRF_TOKEN_MAX_AGE_SECONDS = 24 * 60 * 60; 

    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(ACCESS_TOKEN_PATH)
                .maxAge(ACCESS_TOKEN_MAX_AGE_SECONDS)
                .sameSite(cookieSecure ? "None" : "Lax")
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(REFRESH_TOKEN_PATH)
                .maxAge(REFRESH_TOKEN_MAX_AGE_SECONDS)
                .sameSite(cookieSecure ? "None" : "Lax")
                .build();
    }

    public ResponseCookie createCsrfTokenCookie(String csrfToken) {
        return ResponseCookie.from(CSRF_TOKEN_COOKIE_NAME, csrfToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(CSRF_TOKEN_PATH)
                .maxAge(CSRF_TOKEN_MAX_AGE_SECONDS)
                .sameSite("Lax")
                .build();
    }

    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = createAccessTokenCookie(accessToken);
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = createRefreshTokenCookie(refreshToken);
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void addCsrfTokenCookie(HttpServletResponse response, String csrfToken) {
        ResponseCookie cookie = createCsrfTokenCookie(csrfToken);
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public String getAccessTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(ACCESS_TOKEN_PATH)
                .maxAge(0)
                .sameSite(cookieSecure ? "None" : "Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(REFRESH_TOKEN_PATH)
                .maxAge(0)
                .sameSite(cookieSecure ? "None" : "Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearCsrfTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(CSRF_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(CSRF_TOKEN_PATH)
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
