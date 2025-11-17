package com.lotosia.identityservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author: nijataghayev
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmail(EmailAlreadyInUseException ex, HttpServletRequest req) {
        return buildResponse(
                "EMAIL_ALREADY_EXIST",
                ex.getMessage(),
                HttpStatus.CONFLICT,
                req.getRequestURI()
        );
    }

    @ExceptionHandler({ExpiredOtpException.class, InvalidOtpException.class})
    public ResponseEntity<ApiError> handleOtp(Exception ex, HttpServletRequest req) {

        String code = ex instanceof ExpiredOtpException ? "EXPIRED_OTP" : "INVALID_OTP";

        return buildResponse(
                code,
                ex.getMessage(),
                HttpStatus.CONFLICT,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return buildResponse(
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                HttpStatus.CONFLICT,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiError> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest req) {
        return buildResponse(
                "MANY_REQUESTS",
                ex.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return buildResponse(
                "NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                req.getRequestURI()
        );
    }

    private ResponseEntity<ApiError> buildResponse(
            String code, String message, HttpStatus status, String path) {

        ApiError error = ApiError.builder()
                .code(code)
                .message(message)
                .status(status.value())
                .path(path)
                .build();

        return ResponseEntity.status(status).body(error);
    }
}

