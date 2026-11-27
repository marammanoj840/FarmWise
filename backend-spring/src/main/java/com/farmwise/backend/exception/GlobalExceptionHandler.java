package com.farmwise.backend.exception;

import com.farmwise.backend.dto.ApiErrorResponse;
import com.farmwise.backend.dto.ApiErrorResponse.ValidationErrorItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock utcClock;

    public GlobalExceptionHandler(Clock utcClock) {
        this.utcClock = utcClock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationErrorItem> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ValidationErrorItem(err.getField(), err.getDefaultMessage()))
                .toList();
        return buildError(
                HttpStatus.BAD_REQUEST,
                "validation_error",
                "Request validation failed.",
                request.getRequestURI(),
                errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<ValidationErrorItem> errors = ex.getConstraintViolations().stream()
                .map(v -> new ValidationErrorItem(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return buildError(
                HttpStatus.BAD_REQUEST,
                "constraint_violation",
                "Constraint validation failed.",
                request.getRequestURI(),
                errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "validation_error",
                "Request parameter validation failed.",
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "malformed_request",
                "Malformed or incomplete request payload.",
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "unauthorized",
                ex.getMessage(),
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleSpringAuth(
            AuthenticationException ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "unauthorized",
                "Authentication failed.",
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler({ForbiddenOperationException.class, AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            Exception ex, HttpServletRequest request) {
        String msg = ex instanceof ForbiddenOperationException
                ? ex.getMessage()
                : "You do not have permission to perform this action.";
        return buildError(
                HttpStatus.FORBIDDEN,
                "forbidden",
                msg,
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "not_found",
                ex.getMessage(),
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "not_found",
                "Requested endpoint was not found.",
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.CONFLICT,
                "conflict",
                ex.getMessage(),
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.CONFLICT,
                "data_integrity_conflict",
                "Operation conflicts with existing records or constraints.",
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessValidation(
            BusinessValidationException ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "business_validation_error",
                ex.getMessage(),
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal_error",
                "An unexpected internal error occurred.",
                request.getRequestURI(),
                List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String code,
            String message,
            String path,
            List<ValidationErrorItem> fieldErrors) {
        ApiErrorResponse body = new ApiErrorResponse(
                OffsetDateTime.now(utcClock),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
