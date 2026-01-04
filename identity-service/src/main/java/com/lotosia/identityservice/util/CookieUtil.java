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
    private static final String ACCESS_TOKEN_PATH = "/";
    private static final String REFRESH_TOKEN_PATH = "/";
    private static final int ACCESS_TOKEN_MAX_AGE_SECONDS = 24 * 60 * 60;
    private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 7 * 24 * 60 * 60;

    private ResponseCookie createCookie(String name, String value, boolean httpOnly, String path, int maxAge, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(cookieSecure)
                .path(path)
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }

    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return createCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken, true, ACCESS_TOKEN_PATH,
                          ACCESS_TOKEN_MAX_AGE_SECONDS, cookieSecure ? "None" : "Lax");
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, true, REFRESH_TOKEN_PATH,
                          REFRESH_TOKEN_MAX_AGE_SECONDS, cookieSecure ? "None" : "Lax");
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
        // SECURITY: Dual-Cookie CSRF Pattern
        // HttpOnly cookie for server-side validation (secure, XSS cannot read)
        // Non-HttpOnly cookie for browser JS access (enables CSRF protection)
        // Both cookies have identical values - client reads non-HttpOnly, server validates against HttpOnly

        // HttpOnly CSRF cookie - secure server validation
        ResponseCookie httpOnlyCookie = createCookie("csrfTokenHttpOnly", csrfToken, true, "/",
                                                  24 * 60 * 60, "Lax");
        response.addHeader("Set-Cookie", httpOnlyCookie.toString());

        // JS-readable CSRF cookie - explicit security attributes
        ResponseCookie jsCookie = ResponseCookie.from("csrfToken", csrfToken)
                .httpOnly(false)  // Explicit: JS must be able to read this
                .secure(cookieSecure)  // Secure when HTTPS enabled
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", jsCookie.toString());
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String getAccessTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = createCookie(ACCESS_TOKEN_COOKIE_NAME, "", true, ACCESS_TOKEN_PATH,
                                           0, cookieSecure ? "None" : "Lax");
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, "", true, REFRESH_TOKEN_PATH,
                                           0, cookieSecure ? "None" : "Lax");
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearCsrfTokenCookie(HttpServletResponse response) {
        // Clear HttpOnly CSRF cookie
        ResponseCookie httpOnlyCookie = createCookie("csrfTokenHttpOnly", "", true, "/", 0, "Lax");
        response.addHeader("Set-Cookie", httpOnlyCookie.toString());

        // Clear JS-readable CSRF cookie - explicit security attributes
        ResponseCookie jsCookie = ResponseCookie.from("csrfToken", "")
                .httpOnly(false)  // Explicit: matches creation settings
                .secure(cookieSecure)  // Secure when HTTPS enabled
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", jsCookie.toString());
    }

}
