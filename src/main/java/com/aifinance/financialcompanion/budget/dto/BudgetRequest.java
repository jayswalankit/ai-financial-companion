package com.aifinance.financialcompanion.budget.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetRequest(

        @NotNull(message = "month is required")
        @Min(1)
        @Max(12)
         Integer month,

        @NotNull(message = "year is required")
        @Positive
        Integer year,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.1")
        BigDecimal budgetAmount

        ) {
}
