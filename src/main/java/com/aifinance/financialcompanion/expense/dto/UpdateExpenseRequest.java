package com.aifinance.financialcompanion.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateExpenseRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        String description,

        @NotNull(message = "Expense Date is required")
        LocalDate expenseDate,

        @NotNull(message = "Category Id is required")
        Long categoryId

) {
}
