package com.lotosia.identityservice.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CookieUtil {

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${cookie.domain:#{null}}")
    private String cookieDomain;

    @Value("${cookie.access-token.max-age:86400}")
    private int accessTokenMaxAge;

    @Value("${cookie.refresh-token.max-age:604800}")
    private int refreshTokenMaxAge;

    @Value("${cookie.csrf.max-age:86400}")
    private int csrfMaxAge;

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
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(cookieSecure)
                .path(path)
                .maxAge(maxAge)
                .sameSite(sameSite);

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public int computeCookieMaxAge(long expirationSecondsFromNow) {
        return Math.max(60, Math.min((int) expirationSecondsFromNow, accessTokenMaxAge));
    }

    private String getSameSiteValue() {
        return cookieSecure ? "None" : "Lax";
    }

    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return createCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken, true, ACCESS_TOKEN_PATH,
                          accessTokenMaxAge, getSameSiteValue());
    }

    public ResponseCookie createAccessTokenCookie(String accessToken, int maxAge) {
        return createCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken, true, ACCESS_TOKEN_PATH,
                          maxAge, getSameSiteValue());
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, true, REFRESH_TOKEN_PATH,
                          refreshTokenMaxAge, getSameSiteValue());
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken, int maxAge) {
        return createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, true, REFRESH_TOKEN_PATH,
                          maxAge, getSameSiteValue());
    }

    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = createAccessTokenCookie(accessToken);
        addCookie(response, cookie);
    }

    public void addAccessTokenCookie(HttpServletResponse response, String accessToken, int maxAge) {
        ResponseCookie cookie = createAccessTokenCookie(accessToken, maxAge);
        addCookie(response, cookie);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = createRefreshTokenCookie(refreshToken);
        addCookie(response, cookie);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        ResponseCookie cookie = createRefreshTokenCookie(refreshToken, maxAge);
        addCookie(response, cookie);
    }

    public void addCsrfTokenCookie(HttpServletResponse response, String csrfToken) {
        ResponseCookie httpOnlyCookie = createCookie(CSRF_TOKEN_HTTP_ONLY_COOKIE_NAME, csrfToken, true,
                                                  CSRF_TOKEN_PATH, csrfMaxAge, "Lax");
        addCookie(response, httpOnlyCookie);

        ResponseCookie jsCookie = createCookie(CSRF_TOKEN_JS_COOKIE_NAME, csrfToken, false,
                                            CSRF_TOKEN_PATH, csrfMaxAge, "Lax");
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

    private Map<String, String> getCookieMap(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Map.of();

        Map<String, String> cookieMap = new HashMap<>();
            for (Cookie cookie : cookies) {
            cookieMap.put(cookie.getName(), cookie.getValue());
                }
        return cookieMap;
        }

    public String getAccessTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = createCookie(ACCESS_TOKEN_COOKIE_NAME, "", true, ACCESS_TOKEN_PATH,
                                           0, getSameSiteValue());
        addCookie(response, cookie);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, "", true, REFRESH_TOKEN_PATH,
                                           0, getSameSiteValue());
        addCookie(response, cookie);
    }

    public void clearCsrfTokenCookie(HttpServletResponse response) {
        ResponseCookie httpOnlyCookie = createCookie(CSRF_TOKEN_HTTP_ONLY_COOKIE_NAME, "", true,
                                                  CSRF_TOKEN_PATH, 0, getSameSiteValue());
        addCookie(response, httpOnlyCookie);

        ResponseCookie jsCookie = createCookie(CSRF_TOKEN_JS_COOKIE_NAME, "", false,
                                            CSRF_TOKEN_PATH, 0, getSameSiteValue());
        addCookie(response, jsCookie);
    }

}
