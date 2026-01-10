package com.lotosia.identityservice.exception;

import com.lotosia.identityservice.dto.admin.AdminBootstrapResponse;
import com.lotosia.identityservice.web.ExceptionHandlerUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyInUse(EmailAlreadyInUseException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.conflictError("EMAIL_ALREADY_EXIST", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(ExpiredOtpException.class)
    public ResponseEntity<ApiError> handleExpiredOtp(ExpiredOtpException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.badRequestError("EXPIRED_OTP", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiError> handleInvalidOtp(InvalidOtpException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.badRequestError("INVALID_OTP", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.unauthorizedError("INVALID_CREDENTIALS", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiError> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.tooManyRequestsError("TOO_MANY_REQUESTS", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExists(AlreadyExistsException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.conflictError(ex.getCode(), ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        String code = ex.getCode() != null ? ex.getCode() : "NOT_FOUND";
        return ExceptionHandlerUtils.notFoundError(code, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ExceptionHandlerUtils.badRequestError("VALIDATION_ERROR", errorMessage, req.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest req) {

        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return ExceptionHandlerUtils.badRequestError("VALIDATION_ERROR", errorMessage, req.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {

        String message = "Malformed JSON request or invalid request body format";
        if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            message = "Invalid JSON format in request body";
        }

        return ExceptionHandlerUtils.badRequestError("INVALID_REQUEST_BODY", message, req.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest req) {

        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

        return ExceptionHandlerUtils.badRequestError("MISSING_PARAMETER", message, req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {

        String message = String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName());

        return ExceptionHandlerUtils.badRequestError("INVALID_PARAMETER_TYPE", message, req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {

        return ExceptionHandlerUtils.badRequestError("INVALID_ARGUMENT",
                ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided",
                req.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.forbiddenError("ACCESS_DENIED",
                "You don't have permission to access this resource",
                req.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        String message = String.format("Request method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(),
                String.join(", ", ex.getSupportedMethods() != null ? ex.getSupportedMethods() : new String[]{"POST"}));

        return ExceptionHandlerUtils.methodNotAllowedError("METHOD_NOT_ALLOWED", message, req.getRequestURI());
    }

    @ExceptionHandler(AdminAlreadyExistsException.class)
    public ResponseEntity<AdminBootstrapResponse> handleAdminAlreadyExists(AdminAlreadyExistsException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getResponse());
    }

    @ExceptionHandler(AdminUpgradeException.class)
    public ResponseEntity<AdminBootstrapResponse> handleAdminUpgrade(AdminUpgradeException ex, HttpServletRequest req) {
        return ResponseEntity.ok(ex.getResponse());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.notFoundError("USER_NOT_FOUND", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiError> handleRoleNotFound(RoleNotFoundException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.notFoundError("ROLE_NOT_FOUND", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    public ResponseEntity<ApiError> handlePermissionNotFound(PermissionNotFoundException ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.notFoundError("PERMISSION_NOT_FOUND", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest req) {
        return ExceptionHandlerUtils.internalServerError("INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                req.getRequestURI());
    }

}
