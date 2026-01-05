package com.lotosia.apigateway.exception;

import com.lotosia.apigateway.util.ErrorResponseBuilder;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ApiError> handleWebClientRequestException(WebClientRequestException ex) {
        return ErrorResponseBuilder.serviceUnavailable("Unable to connect to the requested service. Please try again later.");
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> handleWebClientResponseException(WebClientResponseException ex) {
        if (ex.getStatusCode().is5xxServerError()) {
            return ErrorResponseBuilder.serviceUnavailable("The service is experiencing issues. Please try again later.");
        } else if (ex.getStatusCode().is4xxClientError()) {
            return ErrorResponseBuilder.badRequest(ex.getStatusText() + ": " + ex.getMessage());
        }
        return ErrorResponseBuilder.internalServerError("Service communication error");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
        return ErrorResponseBuilder.badRequest("The requested service or resource was not found.");
    }
}