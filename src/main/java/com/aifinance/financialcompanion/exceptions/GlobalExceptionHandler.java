package com.aifinance.financialcompanion.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================================================
    // STANDARD ERROR RESPONSE
    // =========================================================

    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {
    }



    // =========================================================
    // EMAIL ALREADY EXISTS
    // =========================================================

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExist(
            EmailAlreadyExistException exception
    ) {

        log.warn("Email already exists: {}", exception.getMessage());

        return buildError(
                HttpStatus.CONFLICT,
                "Email Already Exists",
                exception.getMessage()
        );
    }



    // =========================================================
    // USER NOT FOUND
    // =========================================================

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



    // =========================================================
    // EXPENSE NOT FOUND
    // =========================================================

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExpenseNotFound(
            ExpenseNotFoundException exception
    ) {

        log.warn("Expense not found: {}", exception.getMessage());

        return buildError(
                HttpStatus.NOT_FOUND,
                "Expense Not Found",
                exception.getMessage()
        );
    }



    // =========================================================
    // CATEGORY NOT FOUND
    // =========================================================

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(
            CategoryNotFoundException exception
    ) {

        log.warn("Category not found: {}", exception.getMessage());

        return buildError(
                HttpStatus.NOT_FOUND,
                "Category Not Found",
                exception.getMessage()
        );
    }



    // =========================================================
    // DUPLICATE CATEGORY NAME
    // =========================================================

    @ExceptionHandler(DuplicateCategoryName.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCategoryName(
            DuplicateCategoryName exception
    ) {

        log.warn("Duplicate category name: {}", exception.getMessage());

        return buildError(
                HttpStatus.CONFLICT,
                "Duplicate Category Name",
                exception.getMessage()
        );
    }



    // =========================================================
    // INVALID DATE RANGE
    // =========================================================

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRange(
            InvalidDateRangeException exception
    ) {

        log.warn("Invalid date range: {}", exception.getMessage());

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Invalid Date Range",
                exception.getMessage()
        );
    }



    // =========================================================
    // MONTHLY BUDGET EXCEPTION
    // =========================================================

    @ExceptionHandler(MonthlyBudgetException.class)
    public ResponseEntity<ErrorResponse> handleMonthlyBudgetException(
            MonthlyBudgetException exception
    ) {

        log.warn("Monthly budget exception: {}", exception.getMessage());

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Monthly Budget Error",
                exception.getMessage()
        );
    }



    // =========================================================
    // ACCESS DENIED
    // =========================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception
    ) {

        log.warn("Access denied: {}", exception.getMessage());

        return buildError(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                exception.getMessage()
        );
    }



    // =========================================================
    // VALIDATION ERRORS
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {

            fieldErrors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        log.warn("Validation failed: {}", fieldErrors);

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "Invalid request body");
        response.put("fieldErrors", fieldErrors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }



    // =========================================================
    // ILLEGAL ARGUMENT EXCEPTION
    // =========================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception
    ) {

        log.warn("Illegal argument: {}", exception.getMessage());

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                exception.getMessage()
        );
    }



    // =========================================================
    // GENERIC EXCEPTION
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception
    ) {

        log.error(
                "Unexpected error occurred: {}",
                exception.getMessage(),
                exception
        );

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Something went wrong. Please try again later."
        );
    }



    // =========================================================
    // COMMON ERROR BUILDER
    // =========================================================

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