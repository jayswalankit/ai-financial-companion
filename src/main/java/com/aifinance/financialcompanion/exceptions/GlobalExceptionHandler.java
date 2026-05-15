package com.aifinance.financialcompanion.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────────────────────
    // Standard Error Response DTO
    // ─────────────────────────────────────────────────────────────

    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {
    }

    // ─────────────────────────────────────────────────────────────
    // Email Already Exists
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExist(
            EmailAlreadyExistException exception
    ) {

        log.warn("Email conflict occurred: {}", exception.getMessage());

        return buildError(
                HttpStatus.CONFLICT,
                "Email Already Exists",
                exception.getMessage()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // User Not Found
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFound exception
    ) {

        log.warn("User not found: {}", exception.getMessage());

        return buildError(
                HttpStatus.NOT_FOUND,
                "User Not Found",
                exception.getMessage()
        );
    }

    // ─────────────────────────────────────────────────────────────
    // Expense Not Found
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleExpenseNotFound(
            ExpenseNotFoundException exception
    ) {

        log.warn("Expense not found: {}", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage(),
                        null
                ));
    }

    // ─────────────────────────────────────────────────────────────
    // Category Not Found
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(
            CategoryNotFoundException exception
    ) {

        log.warn("Category not found: {}", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage(),
                        null
                ));
    }

    // ─────────────────────────────────────────────────────────────
    // Invalid Date Range
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDateRange(
            InvalidDateRangeException exception
    ) {

        log.warn("Invalid date range provided: {}", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage(),
                        null
                ));
    }

    // ─────────────────────────────────────────────────────────────
    // Access Denied
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException exception
    ) {

        log.warn("Access denied: {}", exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorResponse(
                        HttpStatus.FORBIDDEN,
                        exception.getMessage(),
                        null
                ));
    }

    // ─────────────────────────────────────────────────────────────
    // Validation Errors
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed",
                        fieldErrors
                ));
    }

    // ─────────────────────────────────────────────────────────────
    // Generic / Unexpected Exception
    // ─────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception
    ) {

        log.error("Unexpected error occurred: {}", exception.getMessage(), exception);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Something went wrong. Please try again later."
        );
    }

    // ─────────────────────────────────────────────────────────────
    // Common Error Response Builder
    // ─────────────────────────────────────────────────────────────

    private Map<String, Object> errorResponse(
            HttpStatus status,
            String message,
            Map<String, String> fieldErrors
    ) {

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);

        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            response.put("fieldErrors", fieldErrors);
        }

        return response;
    }

    // ─────────────────────────────────────────────────────────────
    // Standard ErrorResponse Builder
    // ─────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String error,
            String message
    ) {

        return new ResponseEntity<>(
                new ErrorResponse(
                        status.value(),
                        error,
                        message,
                        LocalDateTime.now()
                ),
                status
        );
    }
}