package com.lotosia.apigateway.util;

import com.lotosia.apigateway.exception.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ErrorResponseBuilder {

    private ErrorResponseBuilder() {
    }

    public static ResponseEntity<ApiError> buildError(HttpStatus status, String code, String message) {
        ApiError error = ApiError.builder()
                .code(code)
                .message(message)
                .status(status.value())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    public static ResponseEntity<ApiError> buildErrorWithRetryAfter(HttpStatus status, String code, String message, String retryAfter) {
        ApiError error = ApiError.builder()
                .code(code)
                .message(message)
                .status(status.value())
                .build();

        return ResponseEntity.status(status)
                .header("Retry-After", retryAfter)
                .body(error);
    }

    public static ResponseEntity<ApiError> internalServerError(String message) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", message);
    }

    public static ResponseEntity<ApiError> badRequest(String message) {
        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    public static ResponseEntity<ApiError> serviceUnavailable(String message) {
        return buildErrorWithRetryAfter(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", message, "60");
    }

    public static ResponseEntity<ApiError> gatewayTimeout(String message) {
        return buildError(HttpStatus.GATEWAY_TIMEOUT, "GATEWAY_TIMEOUT", message);
    }
}
