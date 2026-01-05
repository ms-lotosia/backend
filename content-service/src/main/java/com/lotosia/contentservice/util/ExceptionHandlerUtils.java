package com.lotosia.contentservice.util;

import com.lotosia.contentservice.exception.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ExceptionHandlerUtils {

    private ExceptionHandlerUtils() {
    }

    public static ResponseEntity<ApiError> buildErrorResponse(String code, String message, HttpStatus status, String path) {
        ApiError error = ApiError.builder()
                .code(code)
                .message(message)
                .status(status.value())
                .path(path)
                .build();

        return ResponseEntity.status(status).body(error);
    }

    public static ResponseEntity<ApiError> badRequestError(String code, String message, String path) {
        return buildErrorResponse(code, message, HttpStatus.BAD_REQUEST, path);
    }

    public static ResponseEntity<ApiError> unauthorizedError(String code, String message, String path) {
        return buildErrorResponse(code, message, HttpStatus.UNAUTHORIZED, path);
    }

    public static ResponseEntity<ApiError> forbiddenError(String code, String message, String path) {
        return buildErrorResponse(code, message, HttpStatus.FORBIDDEN, path);
    }

    public static ResponseEntity<ApiError> notFoundError(String code, String message, String path) {
        return buildErrorResponse(code, message, HttpStatus.NOT_FOUND, path);
    }

    public static ResponseEntity<ApiError> conflictError(String code, String message, String path) {
        return buildErrorResponse(code, message, HttpStatus.CONFLICT, path);
    }

    public static ResponseEntity<ApiError> tooManyRequestsError(String code, String message, String path) {
        return buildErrorResponse(code, message, HttpStatus.TOO_MANY_REQUESTS, path);
    }

    public static ResponseEntity<ApiError> internalServerError(String code, String message, String path) {
        return buildErrorResponse(code, message, HttpStatus.INTERNAL_SERVER_ERROR, path);
    }
}
