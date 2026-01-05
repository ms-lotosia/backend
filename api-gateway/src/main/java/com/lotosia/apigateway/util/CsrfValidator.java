package com.lotosia.apigateway.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CsrfValidator {

    private CsrfValidator() {
    }

    public static Mono<Void> validateCsrfToken(ServerWebExchange exchange, String path, String method) {
        boolean isCsrfExemptedPath = PathValidator.isCsrfExempted(path);
        String cookieCsrfToken = RequestUtils.extractCsrfTokenFromCookies(exchange.getRequest());

        if (cookieCsrfToken != null &&
            (RequestUtils.isStateChangingMethod(method) || (method.equals("GET") && path.equals("/api/v1/auth/me"))) &&
            PathValidator.startsWithApiV1(path) &&
            !isCsrfExemptedPath) {

            String requestCsrfToken = exchange.getRequest().getHeaders().getFirst("X-CSRF-Token");

            if (requestCsrfToken == null || !requestCsrfToken.equals(cookieCsrfToken)) {
                return ResponseUtils.respondWithForbidden(exchange.getResponse());
            }
        }

        return Mono.empty();
    }
}
