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
    private static final String CSRF_TOKEN_HTTP_ONLY_COOKIE_NAME = "csrfTokenHttpOnly";
    private static final String CSRF_TOKEN_JS_COOKIE_NAME = "csrfToken";
    private static final String ACCESS_TOKEN_PATH = "/";
    private static final String REFRESH_TOKEN_PATH = "/";
    private static final String CSRF_TOKEN_PATH = "/";
    private static final int ACCESS_TOKEN_MAX_AGE_SECONDS = 24 * 60 * 60;
    private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 7 * 24 * 60 * 60;
    private static final int CSRF_TOKEN_MAX_AGE_SECONDS = 24 * 60 * 60;

    private ResponseCookie createCookie(String name, String value, boolean httpOnly, String path, int maxAge, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(cookieSecure)
                .path(path)
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader("Set-Cookie", cookie.toString());
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
        addCookie(response, cookie);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = createRefreshTokenCookie(refreshToken);
        addCookie(response, cookie);
    }

    public void addCsrfTokenCookie(HttpServletResponse response, String csrfToken) {
        ResponseCookie httpOnlyCookie = createCookie(CSRF_TOKEN_HTTP_ONLY_COOKIE_NAME, csrfToken, true,
                                                  CSRF_TOKEN_PATH, CSRF_TOKEN_MAX_AGE_SECONDS, "Lax");
        addCookie(response, httpOnlyCookie);

        ResponseCookie jsCookie = createCookie(CSRF_TOKEN_JS_COOKIE_NAME, csrfToken, false,
                                            CSRF_TOKEN_PATH, CSRF_TOKEN_MAX_AGE_SECONDS, "Lax");
        addCookie(response, jsCookie);
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
        addCookie(response, cookie);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, "", true, REFRESH_TOKEN_PATH,
                                           0, cookieSecure ? "None" : "Lax");
        addCookie(response, cookie);
    }

    public void clearCsrfTokenCookie(HttpServletResponse response) {
        ResponseCookie httpOnlyCookie = createCookie(CSRF_TOKEN_HTTP_ONLY_COOKIE_NAME, "", true,
                                                  CSRF_TOKEN_PATH, 0, "Lax");
        addCookie(response, httpOnlyCookie);

        ResponseCookie jsCookie = createCookie(CSRF_TOKEN_JS_COOKIE_NAME, "", false,
                                            CSRF_TOKEN_PATH, 0, "Lax");
        addCookie(response, jsCookie);
    }

}
