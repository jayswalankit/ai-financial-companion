package com.aifinance.financialcompanion.expense.dto;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(

        Long id,

        String title,

        BigDecimal amount,

        String description,

        LocalDate expenseDate,

        String categoryName,

        LocalDateTime createdAt

) {
}
