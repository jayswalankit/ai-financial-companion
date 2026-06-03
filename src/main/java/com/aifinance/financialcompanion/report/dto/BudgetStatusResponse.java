package com.aifinance.financialcompanion.report.dto;

import java.math.BigDecimal;

public record BudgetStatusResponse(
        BigDecimal monthlyBudget,
        BigDecimal currentSpent,
        BigDecimal remainingBudget,
        BigDecimal recommendedLimit,
        String status,
        String advice
) {
}
