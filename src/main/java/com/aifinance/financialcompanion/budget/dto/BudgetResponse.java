package com.aifinance.financialcompanion.budget.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetResponse(
Long id,
Integer month,
Integer year,
BigDecimal budgetAmount,
LocalDateTime createdAt,
LocalDateTime updatedAt
) {
}
