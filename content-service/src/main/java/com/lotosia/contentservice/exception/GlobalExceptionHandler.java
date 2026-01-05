package com.lotosia.contentservice.exception;

import com.lotosia.contentservice.util.ExceptionHandlerUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex, WebRequest request) {
        return ExceptionHandlerUtils.badRequestError("BAD_REQUEST", ex.getMessage(), request.getDescription(false));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex, WebRequest request) {
        return ExceptionHandlerUtils.notFoundError("NOT_FOUND", ex.getMessage(), request.getDescription(false));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        return ExceptionHandlerUtils.notFoundError("RESOURCE_NOT_FOUND", ex.getMessage(), request.getDescription(false));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse(ex.getMessage());

        return ExceptionHandlerUtils.badRequestError("VALIDATION_ERROR", message, request.getDescription(false));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        return ExceptionHandlerUtils.internalServerError("INTERNAL_SERVER_ERROR", "An unexpected error occurred", request.getDescription(false));
    }
}
