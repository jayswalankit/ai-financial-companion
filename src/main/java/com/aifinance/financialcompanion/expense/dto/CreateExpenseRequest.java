package com.aifinance.financialcompanion.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Amount must be required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        String description,

        @NotNull(message = "Expense date is required")
        LocalDate expenseDate,

        @NotNull(message = "CategoryId is required")
        Long categoryId


) {
}
