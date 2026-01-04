package com.lotosia.apigateway.exception;

import org.springframework.http.HttpStatus;
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
        ApiError error = ApiError.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred in the API Gateway")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> handleServerWebInputException(ServerWebInputException ex) {
        ApiError error = ApiError.builder()
                .code("BAD_REQUEST")
                .message("Invalid request format")
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiError error = ApiError.builder()
                .code("BAD_REQUEST")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<ApiError> handleConnectException(ConnectException ex) {
        ApiError error = ApiError.builder()
                .code("SERVICE_UNAVAILABLE")
                .message("The requested service is currently unavailable. Please try again later.")
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "60")
                .body(error);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiError> handleTimeoutException(TimeoutException ex) {
        ApiError error = ApiError.builder()
                .code("GATEWAY_TIMEOUT")
                .message("The request timed out while waiting for the service to respond.")
                .status(HttpStatus.GATEWAY_TIMEOUT.value())
                .build();

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error);
    }
}