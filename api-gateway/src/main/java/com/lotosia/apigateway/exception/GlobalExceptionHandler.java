package com.lotosia.apigateway.exception;

import com.lotosia.apigateway.util.ErrorResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        return ErrorResponseBuilder.internalServerError("An unexpected error occurred in the API Gateway");
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> handleServerWebInputException(ServerWebInputException ex) {
        return ErrorResponseBuilder.badRequest("Invalid request format");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ErrorResponseBuilder.badRequest(ex.getMessage());
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<ApiError> handleConnectException(ConnectException ex) {
        return ErrorResponseBuilder.serviceUnavailable("The requested service is currently unavailable. Please try again later.");
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiError> handleTimeoutException(TimeoutException ex) {
        return ErrorResponseBuilder.gatewayTimeout("The request timed out while waiting for the service to respond.");
    }
}