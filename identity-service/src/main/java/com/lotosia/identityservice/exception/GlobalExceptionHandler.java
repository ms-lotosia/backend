package com.lotosia.identityservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyInUse(EmailAlreadyInUseException ex, HttpServletRequest req) {
        return buildResponse(
                "EMAIL_ALREADY_EXIST",
                ex.getMessage(),
                HttpStatus.CONFLICT,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(ExpiredOtpException.class)
    public ResponseEntity<ApiError> handleExpiredOtp(ExpiredOtpException ex, HttpServletRequest req) {
        return buildResponse(
                "EXPIRED_OTP",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiError> handleInvalidOtp(InvalidOtpException ex, HttpServletRequest req) {
        return buildResponse(
                "INVALID_OTP",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return buildResponse(
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiError> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest req) {
        return buildResponse(
                "TOO_MANY_REQUESTS",
                ex.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        String code = ex.getCode() != null ? ex.getCode() : "NOT_FOUND";
        return buildResponse(
                code,
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return buildResponse(
                "VALIDATION_ERROR",
                errorMessage,
                HttpStatus.BAD_REQUEST,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest req) {
        
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return buildResponse(
                "VALIDATION_ERROR",
                errorMessage,
                HttpStatus.BAD_REQUEST,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        
        String message = "Malformed JSON request or invalid request body format";
        if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            message = "Invalid JSON format in request body";
        }

        return buildResponse(
                "INVALID_REQUEST_BODY",
                message,
                HttpStatus.BAD_REQUEST,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest req) {
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        return buildResponse(
                "MISSING_PARAMETER",
                message,
                HttpStatus.BAD_REQUEST,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        
        String message = String.format("Invalid value '%s' for parameter '%s'", 
                ex.getValue(), ex.getName());
        
        return buildResponse(
                "INVALID_PARAMETER_TYPE",
                message,
                HttpStatus.BAD_REQUEST,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {
        
        logger.warn("Illegal argument exception: {}", ex.getMessage());
        
        return buildResponse(
                "INVALID_ARGUMENT",
                ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided",
                HttpStatus.BAD_REQUEST,
                req.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest req) {
        logger.error("Unexpected error occurred", ex);
        
        return buildResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR,
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
